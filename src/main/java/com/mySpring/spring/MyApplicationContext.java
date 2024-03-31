package com.mySpring.spring;

import java.beans.Beans;
import java.beans.Introspector;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class MyApplicationContext {

    private Class configClass;
    private ConcurrentHashMap<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,Object> singletonBeanMap = new ConcurrentHashMap<>();
    private ArrayList<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();
    public MyApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 扫描
        if(configClass.isAnnotationPresent(ComponentScan.class)){
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            String path = componentScanAnnotation.value();
            path=path.replace(".","/");

            ClassLoader classLoader = MyApplicationContext.class.getClassLoader();

            URL resource = classLoader.getResource(path);

            File file = new File(resource.getFile());
            if(file.isDirectory()){
                File[] files = file.listFiles();

                for(File f:files){
                    String fileName = f.getAbsolutePath();
                    if(fileName.endsWith(".class")){

                        String className = fileName.substring(fileName.indexOf("com"),fileName.indexOf(".class"));
                        className = className.replace("\\",".");
                        try{
                            Class<?> clazz = classLoader.loadClass(className);
                            if(clazz.isAnnotationPresent(Component.class)) {

                                if(BeanPostProcessor.class.isAssignableFrom(clazz)){
                                    BeanPostProcessor instance = (BeanPostProcessor) clazz.newInstance();
                                    beanPostProcessorList.add(instance);
                                }
                                // BeanDefinition
                                Component component = clazz.getAnnotation(Component.class);
                                String componentName = component.value();
                                if(componentName.equals("")){
                                    componentName = Introspector.decapitalize(clazz.getSimpleName());
                                }
                                BeanDefinition beanDefinition = new BeanDefinition();
                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    Scope scopAnnotation = clazz.getAnnotation(Scope.class);
                                    String value = scopAnnotation.value();
                                    beanDefinition.setScope(value);

                                }else{
                                    beanDefinition.setScope("singleton");
                                }
                                beanDefinitionMap.put(componentName,beanDefinition);

                                beanDefinition.setType(clazz);

                            }
                        }catch (ClassNotFoundException e){
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
            }

        }

        //创建单例bean
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope()=="singleton"){
                Object bean = createBean(beanName, beanDefinition);
                singletonBeanMap.put(beanName,bean);
            }

        }
    }

    private  Object createBean(String beanName,BeanDefinition beanDefinition){
        Class clazz = beanDefinition.getType();
        try {
            //实例化
            Object instance = clazz.getConstructor().newInstance();
            //依赖注入
            for (Field field : clazz.getDeclaredFields()) {
                if(field.isAnnotationPresent(Autowired.class)){
                    field.setAccessible(true);
                    field.set(instance,getBean(field.getName()));
                }
            }
            //回调
            if(instance instanceof BeanNameAware){
                ((BeanNameAware)instance).setBeanName(beanName);
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanPostProcessor.postProcessBeforeInitialization(beanName,instance);
            }

            //初始化
            if(instance instanceof InitializingBean){
                ((InitializingBean)instance).afrerPropertiesSet();
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanPostProcessor.postProcessAfterInitialization(beanName,instance);
            }
            return instance;
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

    public Object getBean(String beanname){
        // bean name
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanname);

        if(beanDefinition==null){
            throw new NullPointerException();
        }else{
            String scope = beanDefinition.getScope();
            if(scope.equals("singleton")){
                Object bean = singletonBeanMap.get(beanname);
                if(bean==null){
                    Object bean1 = createBean(beanname, beanDefinition);
                    singletonBeanMap.put(beanname,bean1);
                }
                return bean;

            } else{
                //多例
                return createBean(beanname,beanDefinition);
            }
        }

    }
}
