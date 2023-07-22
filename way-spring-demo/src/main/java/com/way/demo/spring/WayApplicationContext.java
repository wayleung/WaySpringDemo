package com.way.demo.spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WayApplicationContext {

  private Class configClass;

  private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>();
  //单例池
  private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<String, Object>();
  private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<BeanPostProcessor>();

  public WayApplicationContext(Class appConfigClass) {
    this.configClass = configClass;

    //扫描 -->  beanDefinition --> beanDefinitionMap
    if (appConfigClass.isAnnotationPresent(ComponentScan.class)) {
      ComponentScan configClassAnnotation = (ComponentScan) appConfigClass.getAnnotation(
          ComponentScan.class);
      String path = configClassAnnotation.value();
      path = path.replace(".", "/");
//      System.out.println(path);

      //2.扫描@ComponentScan 路径下的.class文件
      URL resource = appConfigClass.getClassLoader().getResource(path);
      String filePath = resource.getFile();
//      System.out.println(filePath);
      File dic = new File(filePath);
      // TODO: 2023/7/22 路径递归
      if (dic.isDirectory()) {
        File[] files = dic.listFiles();
        for (File file : files) {
          String fileName = file.getName();
          if (fileName.endsWith(".class")) {
            //只扫描.class文件
            //根据绝对路径获取className
            String absolutePath = file.getAbsolutePath();
//            System.out.println(absolutePath);
            absolutePath = absolutePath.substring(absolutePath.indexOf("com"),
                absolutePath.indexOf(".class"));
            String className = absolutePath.replace("\\", ".");
//            System.out.println(className);
            //获取class类
            try {
              Class clazz = Class.forName(className);
              //扫描bean
              if (clazz.isAnnotationPresent(Component.class)) {

                //不能用instanceOf 不是实例 是对象
                if(BeanPostProcessor.class.isAssignableFrom(clazz)){
                  BeanPostProcessor beanPostProcessorInstance = (BeanPostProcessor) clazz.newInstance();
                  beanPostProcessorList.add(beanPostProcessorInstance);
                }

                Component clazzAnnotation = (Component) clazz.getAnnotation(Component.class);
                String beanName = clazzAnnotation.value();
                //默认单例
                String scope = "singleton";
                if (clazz.isAnnotationPresent(Scope.class)) {
                  Scope scopeAnnotation = (Scope) clazz.getAnnotation(Scope.class);
                  scope = scopeAnnotation.value();
                }
//                System.out.println(className);
                BeanDefinition beanDefinition = new BeanDefinition();
                beanDefinition.setBeanName(beanName);
                beanDefinition.setClazz(clazz);
                beanDefinition.setScope(scope);
                //保存 beanDefinition 到 beanDefinitionMap
                beanDefinitionMap.put(beanName, beanDefinition);
              }
            } catch (ClassNotFoundException e) {
              throw new RuntimeException(e);
            } catch (InstantiationException e) {


            } catch (IllegalAccessException e) {
              throw new RuntimeException(e);
            }
          }
        }
      }
    }

    //实例化单例bean
    Set<Entry<String, BeanDefinition>> entries = beanDefinitionMap.entrySet();
    for (Entry<String, BeanDefinition> entry : entries) {
      String beanName = entry.getKey();
      BeanDefinition beanDefinition = entry.getValue();
      String scope = beanDefinition.getScope();
      if ("singleton".equals(scope)) {
        Object bean = createBean(beanName, beanDefinition);
        // 把单例对象生成出来 这样getbean获取单例bean就可以直接拿出来
        singletonObjects.put(beanName, bean);
      }
    }


  }

  private Object createBean(String beanName, BeanDefinition beanDefinition) {
    try {
      Class clazz = beanDefinition.getClazz();
      Object beanInstance = clazz.getConstructor().newInstance();

      //依赖注入
      Field[] fields = clazz.getDeclaredFields();
      for (Field field : fields) {
        if(field.isAnnotationPresent(Autowired.class)){
          field.setAccessible(true);
          // TODO: 2023/7/22 autowired byName / byType
          Object fieldBean = getBean(field.getName());
          //bean依赖注入注入
          field.set(beanInstance, fieldBean);
        }
      }

      //aware 回调
      if (beanInstance instanceof BeanNameAware) {
        BeanNameAware beanNameAware = (BeanNameAware) beanInstance;
        beanNameAware.setBeanName(beanName);
      }

      //beanPostProcessor before Initialization
      for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
        beanInstance = beanPostProcessor.postProcessBeforeInitialization(beanName, beanInstance);
      }


      //初始化
      if (beanInstance instanceof InitializingBean) {
        InitializingBean initializingBean = (InitializingBean) beanInstance;
        initializingBean.afterPropertiesSet();
      }

      //beanPostProcessor after Initialization
      for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
        //aop 代理对象
        beanInstance = beanPostProcessor.postProcessAfterInitialization(beanName,beanInstance);
      }

      return beanInstance;
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  public Object getBean(String beanName) {
    BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
    if (beanDefinition == null) {
      return new NullPointerException();
    }
    String scope = beanDefinition.getScope();
    if ("singleton".equals(scope)) {
      //单例
      Object bean = singletonObjects.get(beanName);
      if (bean == null) {
        //单例池没有 创建 而且放到单例池
        bean = createBean(beanName, beanDefinition);
        singletonObjects.put(beanName, bean);
      }
      return bean;
    } else {
      //多例 每次都创建
      return createBean(beanName, beanDefinition);
    }
  }
}
