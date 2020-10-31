package com.jin.aop.demo;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

/**
 * @author wu.jinqing
 * @date 2020年10月13日
 */
@Aspect
//@Configuration
@Component
public class AopConfig {
    @Before("execution(public com.jin.aop.demo.User com.jin.aop.demo.Bootstrap.before(int))")
    public void before()
    {
        System.out.println("This is before.");
    }

    @After("execution(public com.jin.aop.demo.User com.jin.aop.demo.Bootstrap.after(int))")
    public void after()
    {
        System.out.println("This is after.");
    }

    @Around("execution(public com.jin.aop.demo.User com.jin.aop.demo.Bootstrap.around(int))")
    public Object around(ProceedingJoinPoint pjp)
    {
        System.out.println("This is Around before.");

        try {
            // 调用被拦截的方法
            Object res = pjp.proceed();

            System.out.println("This is Around after.");

            return res;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return null;
    }


    @AfterReturning("execution(public com.jin.aop.demo.User com.jin.aop.demo.Bootstrap.afterReturning(int))")
    public void afterReturning()
    {
        System.out.println("This is afterReturning.");
    }

    @AfterThrowing("execution(public com.jin.aop.demo.User com.jin.aop.demo.Bootstrap.afterThrowing(int))")
    public void afterThrowing()
    {
        System.out.println("This is afterThrowing.");
    }

    @Before("execution(public com.jin.aop.demo.User com.jin.aop.demo.Bootstrap.beforeAndafter(int))")
    public void before1()
    {
        System.out.println("This is before1.");
    }

    @After("execution(public com.jin.aop.demo.User com.jin.aop.demo.Bootstrap.beforeAndafter(int))")
    public void after1()
    {
        System.out.println("This is after1.");
    }
}
