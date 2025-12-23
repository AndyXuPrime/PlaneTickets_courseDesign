package com.bighomework.planeTicketWeb.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.bighomework.planeTicketWeb.enums.CabinClass;
import com.bighomework.planeTicketWeb.enums.TicketStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_number") // 关联到 flights 表
    private Flight flight;

    @Column(name = "flight_date")
    private LocalDate flightDate;

    /**
     * 【核心】这个 customer_id 现在既代表乘机人，也代表预订人。
     * 我们将字段名改为 passenger 以在代码层面清晰化其主要职责。
     * 数据库中的列名依然由 @JoinColumn(name = "customer_id") 控制，保持不变。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer passenger;

    @Enumerated(EnumType.STRING)
    @Column(name = "`class`") 
    private CabinClass cabinClass;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;
    

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TicketStatus status;

    @Column(name = "booking_time")
    private LocalDateTime bookingTime;

    // payment_time 字段在您的表结构中存在，添加映射
    @Column(name = "payment_time")
    private LocalDateTime paymentTime;
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketStatusLog> statusLogs;
}