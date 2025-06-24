package com.bighomework.planeTicketWeb.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "airlines")
public class Airline {

    @Id
    @Column(name = "airline_code", nullable = false, columnDefinition = "char(2)") 
    private String airlineCode;

    @Column(name = "airline_name", length = 50)
    private String airlineName;

    @Column(name = "country", length = 30)
    private String country;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "website", length = 100)
    private String website;

    // 一个航空公司可以有多个航班
    @OneToMany(mappedBy = "airline", fetch = FetchType.LAZY)
    private List<Flight> flights;
}
