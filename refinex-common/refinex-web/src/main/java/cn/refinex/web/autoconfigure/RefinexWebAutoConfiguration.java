package cn.refinex.web.autoconfigure;

import cn.refinex.web.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Web 层自动配置类
 * <p>
 * 负责注册全局异常处理器等 Web 基础组件。
 *
 * @author refinex
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class RefinexWebAutoConfiguration {

    /**
     * 注册全局异常处理器
     * <p>
     * 使用 @ConditionalOnMissingBean 允许业务服务通过自定义 Bean 覆盖默认异常处理逻辑。
     *
     * @return GlobalExceptionHandler 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
