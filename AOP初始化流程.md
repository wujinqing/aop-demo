## AOP初始化流程

> 1.实现BeanPostProcessor接口，在AbstractAutoProxyCreator.postProcessBeforeInstantiation()方法中的shouldSkip()方法中扫描并初始化
> 所有的被@Aspect注解修饰的类的所有的AOP拦截器方法。

> 2.在AbstractAutoProxyCreator.postProcessAfterInitialization()方法中的wrapIfNecessary()方法中对每一个bean进行判断，
> 找到合适的AOP拦截器，并创建一个新的包含了AOP拦截器的代理bean替换原来的bean， 代理bean会包含特定的拦截器数组。



### 扫描com.jin.aop.demo.AopConfig

### AbstractAutoProxyCreator.postProcessBeforeInstantiation()，在shouldSkip()方法里面扫描所有被@Aspect修饰的类里面的含AOP注解的方法
``` 
@Override
	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
		Object cacheKey = getCacheKey(beanClass, beanName);

		if (!StringUtils.hasLength(beanName) || !this.targetSourcedBeans.contains(beanName)) {
			if (this.advisedBeans.containsKey(cacheKey)) {
				return null;
			}
			// shouldSkip()方法里面扫描所有被@Aspect修饰的类里面的含AOP注解的方法
			if (isInfrastructureClass(beanClass) || shouldSkip(beanClass, beanName)) {
				this.advisedBeans.put(cacheKey, Boolean.FALSE);
				return null;
			}
		}

		// Create proxy here if we have a custom TargetSource.
		// Suppresses unnecessary default instantiation of the target bean:
		// The TargetSource will handle target instances in a custom fashion.
		TargetSource targetSource = getCustomTargetSource(beanClass, beanName);
		if (targetSource != null) {
			if (StringUtils.hasLength(beanName)) {
				this.targetSourcedBeans.add(beanName);
			}
			Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
			// 	Callback aopInterceptor = new DynamicAdvisedInterceptor(this.advised);  真正的处理aop拦截的类
			Object proxy = createProxy(beanClass, beanName, specificInterceptors, targetSource);
			this.proxyTypes.put(cacheKey, proxy.getClass());
			return proxy;
		}

		return null;
	}

```

### AspectJAwareAdvisorAutoProxyCreator.shouldSkip()
``` 
@Override
	protected boolean shouldSkip(Class<?> beanClass, String beanName) {
		// TODO: Consider optimization by caching the list of the aspect names
		// 查找候选的Advisors：
		List<Advisor> candidateAdvisors = findCandidateAdvisors();
		for (Advisor advisor : candidateAdvisors) {
			if (advisor instanceof AspectJPointcutAdvisor &&
					((AspectJPointcutAdvisor) advisor).getAspectName().equals(beanName)) {
				return true;
			}
		}
		return super.shouldSkip(beanClass, beanName);
	}
	
	
	
```

### 查找候选的Advisors：AnnotationAwareAspectJAutoProxyCreator.findCandidateAdvisors()

``` 
@Override
    	protected List<Advisor> findCandidateAdvisors() {
    		// Add all the Spring advisors found according to superclass rules.
    		// 从spring容器中获取Advisor示例，BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this.beanFactory, Advisor.class, true, false);
    		List<Advisor> advisors = super.findCandidateAdvisors();
    		// Build Advisors for all AspectJ aspects in the bean factory.
    		if (this.aspectJAdvisorsBuilder != null) {
    			advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
    		}
    		return advisors;
    	}
```

### 核心重点！！！扫描自定义的AopConfig中定义的各种Advisor：BeanFactoryAspectJAdvisorsBuilder.buildAspectJAdvisors()

``` 
public List<Advisor> buildAspectJAdvisors() {
		List<String> aspectNames = this.aspectBeanNames;

		if (aspectNames == null) {
			synchronized (this) {
				aspectNames = this.aspectBeanNames;
				if (aspectNames == null) {
					List<Advisor> advisors = new ArrayList<>();
					aspectNames = new ArrayList<>();
					// 获取spring容器中的所有对象(因为获取的是Object.class类型)
					String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
							this.beanFactory, Object.class, true, false);
					for (String beanName : beanNames) {
						if (!isEligibleBean(beanName)) {
							continue;
						}
						// We must be careful not to instantiate beans eagerly as in this case they
						// would be cached by the Spring container but would not have been weaved.
						Class<?> beanType = this.beanFactory.getType(beanName);
						if (beanType == null) {
							continue;
						}
						// 是否有Aspect.class注解修饰
						if (this.advisorFactory.isAspect(beanType)) {
							aspectNames.add(beanName);
							AspectMetadata amd = new AspectMetadata(beanType, beanName);
							if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
								MetadataAwareAspectInstanceFactory factory =
										new BeanFactoryAspectInstanceFactory(this.beanFactory, beanName);
								// 查找被AOP注解修饰了的方法
								List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);
								if (this.beanFactory.isSingleton(beanName)) {
									this.advisorsCache.put(beanName, classAdvisors);
								}
								else {
									this.aspectFactoryCache.put(beanName, factory);
								}
								advisors.addAll(classAdvisors);
							}
							else {
								// Per target or per this.
								if (this.beanFactory.isSingleton(beanName)) {
									throw new IllegalArgumentException("Bean with name '" + beanName +
											"' is a singleton, but aspect instantiation model is not singleton");
								}
								MetadataAwareAspectInstanceFactory factory =
										new PrototypeAspectInstanceFactory(this.beanFactory, beanName);
								this.aspectFactoryCache.put(beanName, factory);
								advisors.addAll(this.advisorFactory.getAdvisors(factory));
							}
						}
					}
					this.aspectBeanNames = aspectNames;
					return advisors;
				}
			}
		}

		if (aspectNames.isEmpty()) {
			return Collections.emptyList();
		}
		List<Advisor> advisors = new ArrayList<>();
		for (String aspectName : aspectNames) {
			List<Advisor> cachedAdvisors = this.advisorsCache.get(aspectName);
			if (cachedAdvisors != null) {
				advisors.addAll(cachedAdvisors);
			}
			else {
				MetadataAwareAspectInstanceFactory factory = this.aspectFactoryCache.get(aspectName);
				advisors.addAll(this.advisorFactory.getAdvisors(factory));
			}
		}
		return advisors;
	}
```


### 查找被AOP注解修饰了的方法：ReflectiveAspectJAdvisorFactory.getAdvisors()

``` 
@Override
	public List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory) {
		Class<?> aspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();
		String aspectName = aspectInstanceFactory.getAspectMetadata().getAspectName();
		validate(aspectClass);

		// We need to wrap the MetadataAwareAspectInstanceFactory with a decorator
		// so that it will only instantiate once.
		MetadataAwareAspectInstanceFactory lazySingletonAspectInstanceFactory =
				new LazySingletonAspectInstanceFactoryDecorator(aspectInstanceFactory);

		List<Advisor> advisors = new ArrayList<>();
		for (Method method : getAdvisorMethods(aspectClass)) {
		   // 找到被AOP注解修饰了的方法
			Advisor advisor = getAdvisor(method, lazySingletonAspectInstanceFactory, advisors.size(), aspectName);
			if (advisor != null) {
				advisors.add(advisor);
			}
		}

		// If it's a per target aspect, emit the dummy instantiating aspect.
		if (!advisors.isEmpty() && lazySingletonAspectInstanceFactory.getAspectMetadata().isLazilyInstantiated()) {
			Advisor instantiationAdvisor = new SyntheticInstantiationAdvisor(lazySingletonAspectInstanceFactory);
			advisors.add(0, instantiationAdvisor);
		}

		// Find introduction fields.
		for (Field field : aspectClass.getDeclaredFields()) {
			Advisor advisor = getDeclareParentsAdvisor(field);
			if (advisor != null) {
				advisors.add(advisor);
			}
		}

		return advisors;
	}
```

### AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod()查找被修饰的AOP注解

``` 

private static final Class<?>[] ASPECTJ_ANNOTATION_CLASSES = new Class<?>[] {
			Pointcut.class, Around.class, Before.class, After.class, AfterReturning.class, AfterThrowing.class};
			
			
			
protected static AspectJAnnotation<?> findAspectJAnnotationOnMethod(Method method) {
		for (Class<?> clazz : ASPECTJ_ANNOTATION_CLASSES) {
			AspectJAnnotation<?> foundAnnotation = findAnnotation(method, (Class<Annotation>) clazz);
			if (foundAnnotation != null) {
				return foundAnnotation;
			}
		}
		return null;
	}
```

### AopUtils.canApply()

```
public static boolean canApply(Pointcut pc, Class<?> targetClass, boolean hasIntroductions) {
		Assert.notNull(pc, "Pointcut must not be null");
		// AOP拦截器的方法的声明类型和当前类是否匹配
		if (!pc.getClassFilter().matches(targetClass)) {
			return false;
		}

		MethodMatcher methodMatcher = pc.getMethodMatcher();
		if (methodMatcher == MethodMatcher.TRUE) {
			// No need to iterate the methods if we're matching any method anyway...
			return true;
		}

		IntroductionAwareMethodMatcher introductionAwareMethodMatcher = null;
		if (methodMatcher instanceof IntroductionAwareMethodMatcher) {
			introductionAwareMethodMatcher = (IntroductionAwareMethodMatcher) methodMatcher;
		}

		Set<Class<?>> classes = new LinkedHashSet<>();
		if (!Proxy.isProxyClass(targetClass)) {
			classes.add(ClassUtils.getUserClass(targetClass));
		}
		classes.addAll(ClassUtils.getAllInterfacesForClassAsSet(targetClass));

		for (Class<?> clazz : classes) {
			Method[] methods = ReflectionUtils.getAllDeclaredMethods(clazz);
			for (Method method : methods) {
				if (introductionAwareMethodMatcher != null ?
						introductionAwareMethodMatcher.matches(method, targetClass, hasIntroductions) :
						methodMatcher.matches(method, targetClass)) {
					return true;
				}
			}
		}

		return false;
	}

```


### AOP查找匹配方法：SignaturePattern.matchesExactlyMethod()

``` 
private FuzzyBoolean matchesExactlyMethod(JoinPointSignature aMethod, World world, boolean subjectMatch) {
		if (parametersCannotMatch(aMethod)) {
			// System.err.println("Parameter types pattern " + parameterTypes + " pcount: " + aMethod.getParameterTypes().length);
			return FuzzyBoolean.NO;
		}
		// OPTIMIZE only for exact match do the pattern match now? Otherwise defer it until other fast checks complete?
		// 方法名称是否匹配
		if (!name.matches(aMethod.getName())) {
			return FuzzyBoolean.NO;
		}
		// Check the throws pattern
		// 方法抛错是否匹配
		if (subjectMatch && !throwsPattern.matches(aMethod.getExceptions(), world)) {
			return FuzzyBoolean.NO;
		}

		// '*' trivially matches everything, no need to check further
		// 方法声明类型是否匹配
		if (!declaringType.isStar()) {
			if (!declaringType.matchesStatically(aMethod.getDeclaringType().resolve(world))) {
				return FuzzyBoolean.MAYBE;
			}
		}

		// '*' would match any return value
		// 返回类型是否匹配
		if (!returnType.isStar()) {
			boolean b = returnType.isBangVoid();
			if (b) {
				String s = aMethod.getReturnType().getSignature();
				if (s.length() == 1 && s.charAt(0) == 'V') {
					// it is void, so not a match
					return FuzzyBoolean.NO;
				}
			} else {
				if (returnType.isVoid()) {
					String s = aMethod.getReturnType().getSignature();
					if (s.length() != 1 || s.charAt(0) != 'V') {
						// it is not void, so not a match
						return FuzzyBoolean.NO;
					}
				} else {
					if (!returnType.matchesStatically(aMethod.getReturnType().resolve(world))) {
						// looking bad, but there might be parameterization to consider...
						if (!returnType.matchesStatically(aMethod.getGenericReturnType().resolve(world))) {
							// ok, it's bad.
							return FuzzyBoolean.MAYBE;
						}
					}
				}
			}
		}

		// The most simple case: pattern is (..) will match anything
		// 方法参数是否匹配
		if (parameterTypes.size() == 1 && parameterTypes.get(0).isEllipsis()) {
			return FuzzyBoolean.YES;
		}

		if (!parameterTypes.canMatchSignatureWithNParameters(aMethod.getParameterTypes().length)) {
			return FuzzyBoolean.NO;
		}

		// OPTIMIZE both resolution of these types and their annotations should be deferred - just pass down a world and do it lower
		// down
		// ResolvedType[] resolvedParameters = world.resolve(aMethod.getParameterTypes());

		ResolvableTypeList rtl = new ResolvableTypeList(world, aMethod.getParameterTypes());
		// Only fetch the parameter annotations if the pointcut is going to be matching on them
		ResolvedType[][] parameterAnnotationTypes = null;
		if (isMatchingParameterAnnotations()) {
			parameterAnnotationTypes = aMethod.getParameterAnnotationTypes();
			if (parameterAnnotationTypes != null && parameterAnnotationTypes.length == 0) {
				parameterAnnotationTypes = null;
			}
		}

		if (!parameterTypes.matches(rtl, TypePattern.STATIC, parameterAnnotationTypes).alwaysTrue()) {
			// It could still be a match based on the generic sig parameter types of a parameterized type
			if (!parameterTypes.matches(new ResolvableTypeList(world, aMethod.getGenericParameterTypes()), TypePattern.STATIC,
					parameterAnnotationTypes).alwaysTrue()) {
				return FuzzyBoolean.MAYBE;
				// It could STILL be a match based on the erasure of the parameter types??
				// to be determined via test cases...
			}
		}

		// check that varargs specifications match
		if (!matchesVarArgs(aMethod, world)) {
			return FuzzyBoolean.MAYBE;
		}

		// passed all the guards..
		return FuzzyBoolean.YES;
	}
```


### 创建代理对象(JdkDynamicAopProxy或者ObjenesisCglibAopProxy)：DefaultAopProxyFactory.createAopProxy()

``` 
public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
		if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
			Class<?> targetClass = config.getTargetClass();
			if (targetClass == null) {
				throw new AopConfigException("TargetSource cannot determine target class: " +
						"Either an interface or a target is required for proxy creation.");
			}
			if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
				return new JdkDynamicAopProxy(config);
			}
			return new ObjenesisCglibAopProxy(config);
		}
		else {
			return new JdkDynamicAopProxy(config);
		}
	}
```






















































































































































































































































































































































































































































































































































