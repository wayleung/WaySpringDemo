package com.way.demo.service;

import com.way.demo.spring.BeanPostProcessor;
import com.way.demo.spring.Component;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class WayBeanPostProcessor implements BeanPostProcessor {

  public Object postProcessBeforeInitialization(String beanName, Object bean) {
    if("userService".equals(beanName)){
      System.out.println("postProcessBeforeInitialization beanName:+"+beanName);
    }
    return bean;
  }

  public Object postProcessAfterInitialization(String beanName, final Object bean) {
    if("userService".equals(beanName)){
      System.out.println("postProcessAfterInitialization beanName:+"+beanName);
      //aop 代理对象
      Object proxyInstance = Proxy.newProxyInstance(WayBeanPostProcessor.class.getClassLoader(),
          bean.getClass().getInterfaces(), new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
              System.out.println("postProcessAfterInitialization 切面逻辑");
              return method.invoke(bean,args);
            }
          });
      return proxyInstance;
    }
    return bean;
  }
}
