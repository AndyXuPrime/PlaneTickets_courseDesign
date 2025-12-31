package com.bighomework.flight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 航班服务启动类
 * 负责航班查询、管理、以及基础资源（机场、航司）的维护
 */
@EnableCaching
@SpringBootApplication(scanBasePackages = {
        "com.bighomework",   // 扫描当前模块的 Bean
        "com.bighomework.common"    // 扫描 common 模块的 Bean (如全局异常处理、工具类)
})
@EnableDiscoveryClient // 开启 Nacos 服务注册与发现
@EnableFeignClients(basePackages = "com.bighomework.common.feign") // 扫描 Common 模块中的 Feign 接口
public class FlightApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightApplication.class, args);
    }

}