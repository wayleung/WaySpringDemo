package com.way.demo.service;

import com.way.demo.spring.WayApplicationContext;

public class Test {

  public static void main(String[] args) {
    WayApplicationContext applicationContext = new WayApplicationContext(AppConfig.class);
//    System.out.println(applicationContext.getBean("userService"));
//    System.out.println(applicationContext.getBean("userService"));
//    System.out.println(applicationContext.getBean("userService"));
//    System.out.println(applicationContext.getBean("userService"));
//    System.out.println(applicationContext.getBean("userService"));

//    UserService userService = (UserService) applicationContext.getBean("userService");
//    System.out.println(userService.test());
//    System.out.println(userService.getOrderService().test());

    UserInterface userService = (UserInterface) applicationContext.getBean("userService");
    System.out.println(userService.test());
  }
}
