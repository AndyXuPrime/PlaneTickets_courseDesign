package com.bighomework.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单服务启动类
 * 负责机票预订、退票、订单查询以及销量统计接口
 */
@SpringBootApplication(scanBasePackages = {
        "com.bighomework.order",    // 扫描本服务代码
        "com.bighomework.common"    // 扫描 Common 模块 (异常处理、工具类)
})
@EnableDiscoveryClient // 注册到 Nacos
@EnableFeignClients(basePackages = "com.bighomework.common.feign") // 扫描 Common 中的 Feign 接口
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}