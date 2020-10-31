package com.jin.aop.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wu.jinqing
 * @date 2020年10月13日
 */
@SpringBootApplication
@RestController
//@EnableAspectJAutoProxy
public class Bootstrap {
    public static void main(String[] args) {
        SpringApplication.run(Bootstrap.class, args);
    }

    @RequestMapping("/getUser")
    public User before(int id)
    {
        User user = new User();

        user.setId(id);
        user.setName("张三");
        user.setAge(10);

        return user;
    }

    @RequestMapping("/getUser2")
    public User before2(int id)
    {
        User user = new User();

        user.setId(id);
        user.setName("李四");
        user.setAge(40);

        return user;
    }

    @RequestMapping("/getUser3")
    public User after(int id)
    {
        User user = new User();

        user.setId(id);
        user.setName("李四3");
        user.setAge(40);

        return user;
    }

    @RequestMapping("/getUser4")
    public User around(int id)
    {
        User user = new User();

        user.setId(id);
        user.setName("李四4");
        user.setAge(40);

        return user;
    }
    @RequestMapping("/getUser5")
    public User afterReturning(int id)
    {
        User user = new User();

        user.setId(id);
        user.setName("李四5");
        user.setAge(40);

        return user;
    }
    @RequestMapping("/getUser6")
    public User afterThrowing(int id) throws Exception
    {
        throw new Exception("afterThrowing");
    }

    @RequestMapping("/getUser7")
    public User beforeAndafter(int id)
    {
        User user = new User();

        user.setId(id);
        user.setName("李四7");
        user.setAge(40);

        return user;
    }
}
