package com.bighomework.planeTicketWeb.dto.admin;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class FlightAdminDTO {
    private String flightNumber;
    private String airlineName;
    private String airlineCode;
    private String departureAirport;
    private String arrivalAirport;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String aircraftModel;
    private Short businessSeats;
    private Short economySeats;
    private BigDecimal basePrice;
}