package cn.refinex.system.infrastructure.config;

import cn.refinex.system.infrastructure.client.user.UserManageHttpClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * HTTP Service Client 配置
 *
 * @author refinex
 */
@Configuration
@ImportHttpServices(group = "refinex-user", types = UserManageHttpClient.class)
public class HttpServiceClientConfiguration {
}
