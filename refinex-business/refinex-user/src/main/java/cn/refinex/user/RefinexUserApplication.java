package cn.refinex.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Refinex User Application
 *
 * @author refinex
 */
@EnableDiscoveryClient
@MapperScan("cn.refinex.user.infrastructure.persistence.mapper")
@SpringBootApplication(scanBasePackages = "cn.refinex")
public class RefinexUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(RefinexUserApplication.class, args);
    }
}
