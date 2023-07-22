package com.way.demo.service;


import com.way.demo.spring.Autowired;
import com.way.demo.spring.BeanNameAware;
import com.way.demo.spring.BeanPostProcessor;
import com.way.demo.spring.Component;
import com.way.demo.spring.InitializingBean;

@Component("userService")
//@Scope("prototype")
public class UserService implements UserInterface , BeanNameAware, InitializingBean {
  @Autowired
  private OrderService orderService;

  private String beanName;

  private String bizName;

  public String test(){
    return "UserService test beanName:"+beanName;
  }

  public OrderService getOrderService() {
    return orderService;
  }

  public void setOrderService(OrderService orderService) {
    this.orderService = orderService;
  }

  public void setBeanName(String beanName) {
    this.beanName =  beanName;
  }

  public void afterPropertiesSet() {
    bizName = "bizName";
    System.out.println("afterPropertiesSet biz:"+bizName);
  }
}
