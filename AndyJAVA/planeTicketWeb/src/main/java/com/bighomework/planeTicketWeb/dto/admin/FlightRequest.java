package com.bighomework.planeTicketWeb.dto.admin;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class FlightRequest {
    @NotBlank @Size(max = 6)
    private String flightNumber;

    @NotBlank @Size(max = 2)
    private String airlineCode;

    @NotBlank
    private String departureAirport;

    @NotBlank
    private String arrivalAirport;

    @NotNull
    private LocalTime departureTime;

    @NotNull
    private LocalTime arrivalTime;

    private String aircraftModel;

    @NotNull @Min(0)
    private Short businessSeats;

    @NotNull @Min(0)
    private Short economySeats;

    @NotNull @DecimalMin("0.0")
    private BigDecimal basePrice;
}