package com.bighomework.planeTicketWeb.dto.responseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TicketVO {
    private Long ticketId;
    private String bookingReference;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private LocalDate flightDate;
    private String passengerName;
    private String cabinClass;
    private BigDecimal finalPrice;
    private String status;
    private LocalDateTime bookingTime;
}