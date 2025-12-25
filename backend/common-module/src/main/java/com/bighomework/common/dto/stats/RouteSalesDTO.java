package com.bighomework.common.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class RouteSalesDTO {
    private String departureAirport;
    private String arrivalAirport;
    private Long ticketCount;
    private BigDecimal totalRevenue;
}