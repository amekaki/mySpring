package com.mySpring.service;

import com.mySpring.spring.BeanPostProcessor;
import com.mySpring.spring.Component;

@Component
public class myBeanPostProcessor implements BeanPostProcessor {
    @Override
    public void postProcessBeforeInitialization(String beanName, Object bean) {
        if(beanName=="userService"){
            System.out.println("userService11");
        }
    }

    @Override
    public void postProcessAfterInitialization(String beanName, Object bean) {
        if(beanName=="userService"){
            System.out.println("userService22");
        }

    }
}
