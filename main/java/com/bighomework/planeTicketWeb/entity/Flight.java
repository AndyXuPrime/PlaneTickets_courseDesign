package com.bighomework.planeTicketWeb.entity;

import java.math.BigDecimal;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "flights")
public class Flight {

    @Id
    @Column(name = "flight_number", length = 6, nullable = false)
    private String flightNumber;

    // 多对一关系：多个航班属于一个航空公司
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airline_code", referencedColumnName = "airline_code")
    private Airline airline;

    @Column(name = "departure_airport", length = 50)
    private String departureAirport;

    @Column(name = "arrival_airport", length = 50)
    private String arrivalAirport;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    @Column(name = "aircraft_model", length = 20)
    private String aircraftModel;

    @Column(name = "business_seats")
    private Short businessSeats;

    @Column(name = "economy_seats")
    private Short economySeats;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

}