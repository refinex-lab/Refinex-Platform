package cn.refinex.auth.infrastructure.config;

import cn.refinex.auth.infrastructure.client.user.UserHttpClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * HTTP Service Client 注册
 *
 * @author refinex
 */
@Configuration
@ImportHttpServices(group = "refinex-user", types = UserHttpClient.class)
public class HttpServiceClientConfiguration {
}
