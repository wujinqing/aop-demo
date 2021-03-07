package com.jin.aop.demo.test;

/**
 * @author wu.jinqing
 * @date 2021年02月07日
 */
public class Client {
    public static void main(String[] args) {
        Service1 service1 = new Service1();

        System.out.println(service1.dosomething());
    }
}
