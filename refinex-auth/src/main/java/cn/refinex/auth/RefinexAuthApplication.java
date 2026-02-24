package cn.refinex.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Refinex Auth Application
 *
 * @author refinex
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "cn.refinex.auth.infrastructure.client")
@SpringBootApplication(scanBasePackages = "cn.refinex")
public class RefinexAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(RefinexAuthApplication.class, args);
    }
}
