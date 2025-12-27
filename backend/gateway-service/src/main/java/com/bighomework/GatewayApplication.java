package com.bighomework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(
        exclude = {DataSourceAutoConfiguration.class},
        // 【关键】只扫描网关需要的组件，不要扫描 common.exception，防止 500 错误被隐藏
        scanBasePackages = {
                "com.bighomework.filter",
                "com.bighomework.config",
                "com.bighomework.common.security" // 只需要 JWT 工具类
        }
)
@EnableDiscoveryClient
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}