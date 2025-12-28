package com.bighomework.common.dto.responseDTO;

import java.math.BigDecimal;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 航班查询结果视图对象
 * 包含了基础信息以及动态计算出的价格和余票
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchVO {
    // --- 基础信息 ---
    private String flightNumber;
    private String airlineName;
    private String departureAirport;
    private String arrivalAirport;
    private LocalTime departureTime;
    private LocalTime arrivalTime;

    // --- 动态展示信息 (通常展示经济舱，若无票则展示商务舱) ---
    private BigDecimal price;            // 当前显示的票价
    private Integer remainingSeats;      // 当前显示的余票数
    private String cabinClassForDisplay; // 当前显示的舱位名称 (如 "经济舱")

    // --- 详细库存信息 (关键新增：供前端预订页面判断舱位是否可选) ---
    private Integer economyRemainingSeats;  // 经济舱剩余座位
    private Integer businessRemainingSeats; // 商务舱剩余座位
}