package com.bighomework.common.feign;

import com.bighomework.common.enums.CabinClass;
import com.bighomework.common.util.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * 订单服务远程调用接口
 * name = "order-service": 指定要调用的服务名称（必须与 order-service 的 bootstrap.yml 中的 name 一致）
 * path = "/api/tickets": 指定统一的请求前缀
 */
@FeignClient(name = "order-service", path = "/api/tickets")
public interface OrderFeignClient {

    /**
     * 查询某航班、某日期、某舱位的已售座位数
     * 对应 Order 服务中需要实现的 Controller 接口
     *
     * @param flightNumber 航班号
     * @param flightDate   航班日期 (格式: yyyy-MM-dd)
     * @param cabinClass   舱位等级
     * @return 已售数量
     */
    @GetMapping("/sold-count")
    ApiResponse<Integer> getSoldSeatsCount(
            @RequestParam("flightNumber") String flightNumber,
            @RequestParam("flightDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate flightDate,
            @RequestParam("cabinClass") CabinClass cabinClass
    );

}