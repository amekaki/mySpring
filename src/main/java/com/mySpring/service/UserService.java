package com.mySpring.service;

import com.mySpring.spring.*;

@Component("userService")
@Scope("prototype")
public class UserService implements BeanNameAware , InitializingBean {
    private String beanName;
    @Autowired
    private OrderService orderService;

    private String ftest;

    @Override
    public void setBeanName(String beanName){
        this.beanName=beanName;
    }

    @Override
    public void afrerPropertiesSet(){
        ftest = "Success";
    }

    public void getFTest(){

    }
    public void test(){
        System.out.println(orderService);
        System.out.println(beanName);
        System.out.println(ftest);
    }

}
