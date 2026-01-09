package com.bighomework.flight.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.util.List;

@Data
@Entity
@Table(name = "airlines")
@ToString(exclude = "flights")
public class Airline {
    @Id
    @Column(name = "airline_code", length = 2, columnDefinition = "char(2)")
    private String airlineCode;

    private String airlineName;
    private String country;
    private String contactPhone;
    private String website;
    private String logoUrl;
    @JsonIgnore
    @OneToMany(mappedBy = "airline", fetch = FetchType.LAZY)
    private List<Flight> flights;
}