package com.bighomework.common.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteAnalysisDTO {
    private String departureAirport;
    private String arrivalAirport;
    private Long ticketCount;
}