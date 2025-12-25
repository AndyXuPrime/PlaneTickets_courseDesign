package com.bighomework.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 认证服务启动类
 * 负责用户注册、登录认证、Token生成以及用户权限管理
 */
@SpringBootApplication(scanBasePackages = {
        "com.bighomework.auth",     // 扫描当前模块
        "com.bighomework.common"    // 扫描 common 模块 (获取 JwtTokenProvider, GlobalExceptionHandler 等)
})
@EnableDiscoveryClient // 开启 Nacos 服务注册
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

}