package com.bighomework.planeTicketWeb.dto.responseDTO;

import java.math.BigDecimal;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    private String cabinClassForDisplay;

}