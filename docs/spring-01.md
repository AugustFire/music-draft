# 1.Bean的生命周期

1.Bean的初始化销毁方法
```text
    1.注解指定-initMethod/destroyMethod

            @Bean(initMethod = "init",destroyMethod = "destroyM")
            public Person person() {
                return new Person("Tom", 19);
            }

    2.实现接口-InitializingBean, DisposableBean

    3.JSR250 注解@PostConstruct @PreDestroy
    
    4.定义BeanPostProcessor
        public interface BeanPostProcessor {
        	
        
            //任何初始化方法调用前
        	Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException;
        	
            //任何初始化方法调用后
        	Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException;
        
        }

执行顺序: 4 > 3 > 2 > 1 
```

# 2.Aop的原理
流程
```text
EnableAspectJAutoProxy -> @Import(AspectJAutoProxyRegistrar.class) -> 注册AnnotationAwareAspectJAutoProxyCreator("internalAutoProxyCreator")
```
## 1.解析AnnotationAwareAspectJAutoProxyCreator

1.AnnotationAwareAspectJAutoProxyCreator的创建与注册

```text
        1.创建:   流程同Spring Bean
        2.注册:   BeanFactory注册这个BeanPostProcessor
```

2.AnnotationAwareAspectJAutoProxyCreator作用
```text
            在crateBean->doCreateBean之间插入了
            //给了BeanPostProcessors一个机会去返回一个目标bean实例的代理
			Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
            
```
3.resolveBeforeInstantiation
```txt
	protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) {
		for (BeanPostProcessor bp : getBeanPostProcessors()) {
            //如果是 InstantiationAwareBeanPostProcessor
            //则调用 postProcessBeforeInstantiation(显然AnnotationAwareAspectJAutoProxyCreator符合InstantiationAwareBeanPostProcessor)
			if (bp instanceof InstantiationAwareBeanPostProcessor) {
				InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
				Object result = ibp.postProcessBeforeInstantiation(beanClass, beanName);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}
```
4.AnnotationAwareAspectJAutoProxyCreator.postProcessAfterInitialization
```text
wrapIfNecessary(bean, beanName, cacheKey) 
↓
Object proxy = createProxy(bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
↓
createAopProxy(AdvisedSupport config) throws AopConfigException {
		if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
			Class<?> targetClass = config.getTargetClass();
			if (targetClass == null) {
				throw new AopConfigException("TargetSource cannot determine target class: " +
						"Either an interface or a target is required for proxy creation.");
			}
			if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
				return new JdkDynamicAopProxy(config);
			}
            // CGLIB
			return new ObjenesisCglibAopProxy(config);
		}
		else {
            // JDK代理
			return new JdkDynamicAopProxy(config);
		}
	}
```
# 3.Spring tx