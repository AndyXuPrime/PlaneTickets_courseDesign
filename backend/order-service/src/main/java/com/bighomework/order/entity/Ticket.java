package com.bighomework.order.entity;

import com.bighomework.common.enums.CabinClass;
import com.bighomework.common.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    // 【修改】不再引用 Flight 实体，只存航班号
    @Column(name = "flight_number", length = 6, nullable = false)
    private String flightNumber;

    @Column(name = "flight_date", nullable = false)
    private LocalDate flightDate;

    // 【修改】不再引用 Customer 实体，只存用户ID (对应 users 表主键)
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "class", nullable = false)
    private CabinClass cabinClass;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TicketStatus status;

    @Column(name = "booking_time")
    private LocalDateTime bookingTime;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    // 日志属于 Order 服务内部，可以保留级联
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketStatusLog> statusLogs;
}