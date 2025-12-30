package com.bighomework.order.entity;

import com.bighomework.common.enums.CabinClass;
import com.bighomework.common.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "flight_number", length = 6, nullable = false)
    private String flightNumber;

    @Column(name = "flight_date", nullable = false)
    private LocalDate flightDate;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "passenger_name", length = 50, nullable = false)
    private String passengerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "`class`", nullable = false)
    private CabinClass cabinClass;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    private LocalDateTime bookingTime;
    private LocalDateTime paymentTime;
}