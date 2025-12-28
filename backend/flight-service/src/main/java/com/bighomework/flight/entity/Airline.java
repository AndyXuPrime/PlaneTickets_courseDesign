package com.bighomework.flight.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.util.List;

@Data
@Entity
@Table(name = "airlines")
@ToString(exclude = "flights") // 防止 Lombok 打印时死循环
public class Airline {
    @Id
    @Column(name = "airline_code", length = 2, columnDefinition = "char(2)")
    private String airlineCode;

    private String airlineName;
    private String country;
    private String contactPhone;
    private String website;
    private String logoUrl;

    @JsonIgnore // 【核心修复】管理端查询航班时，不需要反向查询航司下的所有航班列表
    @OneToMany(mappedBy = "airline", fetch = FetchType.LAZY)
    private List<Flight> flights;
}