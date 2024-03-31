package com.mySpring.service;
import com.mySpring.spring.MyApplicationContext;
import  com.mySpring.service.UserService;
public class Test {
    public static void main(String[] args) {
        //
        MyApplicationContext applicationContext = new MyApplicationContext(AppConfig.class);

        UserService userService =(UserService) applicationContext.getBean("userService");

        userService.test();

    }
}
