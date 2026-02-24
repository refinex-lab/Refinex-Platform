package cn.refinex.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Refinex System Application
 *
 * @author refinex
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "cn.refinex.system.infrastructure.client")
@MapperScan("cn.refinex.system.infrastructure.persistence.mapper")
@SpringBootApplication(scanBasePackages = "cn.refinex")
public class RefinexSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(RefinexSystemApplication.class, args);
    }
}
