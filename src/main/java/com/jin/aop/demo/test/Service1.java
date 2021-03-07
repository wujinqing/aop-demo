package com.jin.aop.demo.test;

/**
 * @author wu.jinqing
 * @date 2021年02月07日
 */
public class Service1 {
    public String dosomething()
    {

            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                String className = stackTraceElement.getClassName();

                if ("com.jin.aop.demo.test.Client".equals(className)) {
                    return "from com.jin.aop.demo.test.Client";
                }

                if ("com.jin.aop.demo.test.Client2".equals(className)) {
                    return "from com.jin.aop.demo.test.Client2";
                }
            }


        return "";
    }
}
