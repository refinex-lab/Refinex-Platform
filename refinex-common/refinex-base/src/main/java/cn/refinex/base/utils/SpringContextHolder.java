package cn.refinex.base.utils;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * Spring 上下文持有者
 * <p>
 * 通过实现 {@link ApplicationContextAware} 接口，在 Spring 初始化时将 ApplicationContext 注入到静态变量中。
 * 解决在静态工具类、领域模型等非 Spring 管理的类中无法直接获取 Bean 的问题。
 *
 * @author refinex
 */
@Slf4j
public class SpringContextHolder implements ApplicationContextAware {

    /**
     * Spring 全局上下文
     */
    private static ApplicationContext applicationContext;

    /**
     * 实现 {@link ApplicationContextAware} 接口的回调方法
     * <p>
     * 通过此方法将容器环境注入到静态持有对象中。
     *
     * @param applicationContext Spring 上下文对象
     * @throws BeansException 抛出 Bean 异常
     */
    @Override
    @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        // 使用类名显式访问静态变量
        SpringContextHolder.applicationContext = applicationContext;
        log.info("SpringContextHolder initialized successfully, applicationContext is linked.");
    }

    /**
     * 获取 Spring 上下文对象
     *
     * @return {@link ApplicationContext}
     * @throws IllegalStateException 如果上下文尚未初始化则抛出异常
     */
    public static ApplicationContext getApplicationContext() {
        assertContextInjected();
        return applicationContext;
    }

    /**
     * 根据类型获取 Bean
     *
     * @param requiredType Bean 的 Class 类型
     * @param <T>          泛型标记
     * @return Bean 实例
     */
    public static <T> T getBean(Class<T> requiredType) {
        assertContextInjected();
        return applicationContext.getBean(requiredType);
    }

    /**
     * 根据名称获取 Bean
     *
     * @param name Bean 的名称
     * @param <T>  泛型标记
     * @return Bean 实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        assertContextInjected();
        return (T) applicationContext.getBean(name);
    }

    /**
     * 获取指定类型的所有 Bean 实例列表
     *
     * @param type 接口或父类类型
     * @param <T>  泛型标记
     * @return 包含 Bean 名称和实例的 Map 映射
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        assertContextInjected();
        return applicationContext.getBeansOfType(type);
    }

    /**
     * 清理上下文持有者
     * <p>
     * 通常在容器关闭（ContextClosedEvent）时调用，以释放静态引用，防止内存泄漏。
     */
    public static void clearHolder() {
        if (log.isDebugEnabled()) {
            log.debug("Cleaning ApplicationContext in SpringContextHolder: {}", applicationContext);
        }
        applicationContext = null;
    }

    /**
     * 断言上下文是否已注入
     * * @throws IllegalStateException 注入检查失败时抛出
     */
    private static void assertContextInjected() {
        if (applicationContext == null) {
            log.error("applicationContext is null. Possible reasons: 1. SpringContextHolder bean not registered. 2. Method called before Spring startup.");
            throw new IllegalStateException("SpringContextHolder is not initialized. Check your @Configuration or ComponentScan.");
        }
    }
}
