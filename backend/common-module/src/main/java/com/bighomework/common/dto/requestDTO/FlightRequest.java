package com.bighomework.common.dto.requestDTO;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class FlightRequest {
    private String flightNumber;
    private String airlineCode; // 航司管理员此项由后端自动填充
    private String departureAirport;
    private String arrivalAirport;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String aircraftModel;
    private Short businessSeats;
    private Short economySeats;
    private BigDecimal basePrice;
}