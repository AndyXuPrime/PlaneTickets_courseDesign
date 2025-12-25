package com.bighomework.flight.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @Column(name = "logo_url") // 新增字段
    private String logoUrl;

    @JsonIgnore
    @OneToMany(mappedBy = "airline", fetch = FetchType.LAZY)
    private List<Flight> flights;
}