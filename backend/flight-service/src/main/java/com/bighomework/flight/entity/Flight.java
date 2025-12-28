package com.bighomework.flight.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "flights")
public class Flight {
    @Id
    @Column(name = "flight_number", length = 6)
    private String flightNumber;

    @ManyToOne(fetch = FetchType.EAGER) // 改为 EAGER，确保转换 VO 时航司信息已加载
    @JoinColumn(name = "airline_code", referencedColumnName = "airline_code")
    @JsonIgnoreProperties({"flights", "hibernateLazyInitializer", "handler"})
    private Airline airline;

    // 增加基础字段的非空获取方法，防止 NPE
    public BigDecimal getBasePrice() {
        return basePrice == null ? BigDecimal.ZERO : basePrice;
    }
    public Short getEconomySeats() {
        return economySeats == null ? 0 : economySeats;
    }
    public Short getBusinessSeats() {
        return businessSeats == null ? 0 : businessSeats;
    }

    private String departureAirport;
    private String arrivalAirport;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String aircraftModel;
    private Short businessSeats;
    private Short economySeats;
    private BigDecimal basePrice;
}