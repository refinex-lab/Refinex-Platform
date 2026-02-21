package cn.refinex.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Refinex AI Application
 *
 * @author refinex
 */
@EnableDiscoveryClient
@MapperScan("cn.refinex.ai.infrastructure.persistence.mapper")
@SpringBootApplication(scanBasePackages = "cn.refinex")
public class RefinexAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RefinexAiApplication.class, args);
    }
}
