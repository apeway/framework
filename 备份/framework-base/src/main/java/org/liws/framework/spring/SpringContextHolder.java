package org.liws.framework.spring;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.beanutils.MethodUtils;
import org.liws.framework.exception.BusinessRuntimeException;
import org.liws.framework.log.BQLogger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public class SpringContextHolder implements ApplicationContextAware,BeanFactoryPostProcessor {

	private final static Map<ClassLoader, ApplicationContext> applicationContexts = new ConcurrentHashMap<>();
	private final static Map<ClassLoader, DefaultListableBeanFactory> beanFactorys = new ConcurrentHashMap<>();
	
	private static String currentModuleContext = System.getProperty("CURRENT_MODULE_CONTEXT");

	public static String getContextName(){
		ApplicationContext context = getContext();
		if(context == null){
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				String className = loader.getClass().getName();
				if (className.startsWith("org.apache.catalina.loader")){
						try {
							Class<?> webappClassLoader = loader.getClass();
							Method contextMethod = webappClassLoader.getMethod("getContextName");
							Object contextName = contextMethod.invoke(loader);
							if (contextName != null){
								// 判断contextName是否有对应的Logger,若没有生成默认
								String contextStr = contextName.toString();
								if(!contextStr.startsWith("/")){
									return "/"+contextName;
								}else{
									return contextStr;
								}
							}
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException t) {
							BQLogger.error("get context error",t);
						}
					}
					return "/";
		}else{
			return getContextName(context);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public  static <T>  T getCrossContextBean(Class<T> clazz, String contextName) throws BusinessRuntimeException {
		if(contextName == null || contextName.trim().length() == 0){
			throw new  IllegalArgumentException("contextName can not be empty");
		}
		if(!clazz.isInterface()){
			throw new  IllegalArgumentException("clazz must is an interface");
		}
		Set<ClassLoader> keySet = applicationContexts.keySet();
		for(java.util.Iterator<ClassLoader> it = keySet.iterator();it.hasNext(); ){
			ClassLoader classLoader = it.next();
			try {
				if(Thread.currentThread().getContextClassLoader() == classLoader 
						&& contextName.equals(getContextName(applicationContexts.get(classLoader)))){
					return getBean(clazz);
				}else if(contextName.equals(getCrossContextName(classLoader))){
					Class<?> corssContextClazz = Class.forName(clazz.getName(), false, classLoader);
					final Object corssContextBean = MethodUtils.invokeMethod(applicationContexts.get(classLoader), "getBean", new Object[]{corssContextClazz});
					Object proxy = Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz} , new BQInvocationHandler(corssContextBean, clazz.getClassLoader(),classLoader,getContextName()));
					return (T) proxy;
				}
				
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
				BQLogger.error(e);
				throw new BusinessRuntimeException("SpringContextHolder getCrossContextBean error",e);
			}
			
		}
		return null;
	}

	public static void sendStaticMethodNote(String clazzName,String methodName,Object[] parameters){
		Set<ClassLoader> keySet = applicationContexts.keySet();
		for(java.util.Iterator<ClassLoader> it = keySet.iterator();it.hasNext(); ){
			ClassLoader classLoader = it.next();
			try {
				if(Thread.currentThread().getContextClassLoader() != classLoader){
					Class<?> clazz = ClassUtils.forName(clazzName, classLoader);
					MethodUtils.invokeStaticMethod(clazz, methodName, parameters);
				}
				
			} catch (Exception e) {
				BQLogger.error(e);
			}
			
		}
	}

	public static ClassLoader getClassLoaderByContextName(String contextName){
		if(contextName == null || contextName.trim().length() == 0){
			throw new  IllegalArgumentException("contextName can not be empty");
		}
		Set<ClassLoader> keySet = applicationContexts.keySet();
		for(java.util.Iterator<ClassLoader> it = keySet.iterator();it.hasNext(); ){
			ClassLoader classLoader = it.next();
			try {
				if(Thread.currentThread().getContextClassLoader() == classLoader){
					String ct = getContextName(applicationContexts.get(classLoader));
					if(contextName.equals(ct)){
						return classLoader;
					}
				}else{
					Object ct = getCrossContextName(classLoader);
					if(contextName.equals(ct)){
						return classLoader;
					}
				}
				
			} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				BQLogger.error(e);
			}
			
		}
		return null;
	}


	private static Object getCrossContextName(ClassLoader classLoader)
			throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Class<?> clazz = Class.forName(SpringContextHolder.class.getName(), false, classLoader);
		Object holder = MethodUtils.invokeMethod(applicationContexts.get(classLoader), "getBean", new Object[]{clazz});
		Object ct = MethodUtils.invokeMethod(holder, "getContextName", new Object[]{applicationContexts.get(classLoader)});
		return Objects.toString(ct);
	}
	

	public static String getContextName(ApplicationContext applicationContext){
		if(applicationContext != null){
			javax.servlet.ServletContext servletContext = applicationContext.getBean(javax.servlet.ServletContext.class);
			if(servletContext != null){
				String name = servletContext.getContextPath();
				if(servletContext.getClass().getName().contains("Mock") && !StringUtils.hasText(name) && currentModuleContext != null){
					 if(currentModuleContext.startsWith("/")){
						 return currentModuleContext;
					 }else{
						 return "/"+currentModuleContext;
					 }
				}else{
					return name;
				}
			}
		}
		return "/";
	}
	
	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		synchronized(ClassLoader.class){
			applicationContexts.put(Thread.currentThread().getContextClassLoader(), arg0);
		}
	}
	
	public static ApplicationContext getContext(){
		return applicationContexts.get(Thread.currentThread().getContextClassLoader());
	}
	
	public static ApplicationContext getContext(String context){
		Optional<ApplicationContext> find = applicationContexts.values().stream().filter(e -> context.equals(e.getBean(javax.servlet.ServletContext.class).getContextPath())).findAny();
		return find.orElse(getContext());
	}
	
	
	public static EntityManager getEntityManager(){
        EntityManagerFactory emf = (EntityManagerFactory) getBean("entityManagerFactory");
		return emf.createEntityManager();
	}
	
	public static Object getBean(String context, String id){
		ApplicationContext applicationContext = getContext(context);
		if(applicationContext != null){
			return applicationContext.getBean(id);
		}else{
			return null;
		}
		
	}
	public static <T> T getBean(String context, Class<T> clazz){
		ApplicationContext applicationContext = getContext(context);
		if(applicationContext != null){
			return applicationContext.getBean(clazz);
		}else{
			return null;
		}

	}
	
	public static Object getBean(String id){
		ApplicationContext applicationContext = getContext();
		if(applicationContext != null){
			return applicationContext.getBean(id);
		}else{
			return null;
		}
		
	}
	
	public  static <T>  T getBean(Class<T> clazz){
		ApplicationContext applicationContext = getContext();
		if(applicationContext != null){
			return applicationContext.getBean(clazz);
		}else{
			return null;
		}
		
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		beanFactorys.put(Thread.currentThread().getContextClassLoader(), (DefaultListableBeanFactory) beanFactory);
		
	}

	public static DefaultListableBeanFactory getBeanFactory() {
		return beanFactorys.get(Thread.currentThread().getContextClassLoader());
	}

	public static boolean hasClassLoader(ClassLoader contextClassLoader) {
		return applicationContexts.containsKey(contextClassLoader);
	}
	
	/**
	 * 设置当前模块，用于测试时设置，正常环境可以自动获得
	 * @param moduleContext 模块的上下文
	 * @return
	 */
	public static void setCurrentModule(String moduleContext){
		SpringContextHolder.currentModuleContext = moduleContext;
	}
}