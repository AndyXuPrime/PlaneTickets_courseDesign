package com.bighomework.planeTicketWeb.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchVO {
    private String flightNumber;
    private String airlineName;
    private String departureAirport;
    private String arrivalAirport;
    private LocalTime departureTime;
    private LocalTime arrivalTime;

    // 以下字段代表了前端最终展示的价格和余票信息
    private BigDecimal price;
    private Integer remainingSeats;
    
    /**
     * 新增字段：用于告知前端当前展示的价格是属于哪个舱位的。
     * 例如："经济舱", "商务舱", "无票"
     */
    private String cabinClassForDisplay; 

}