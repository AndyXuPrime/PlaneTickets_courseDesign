package com.bighomework.planeTicketWeb.entity;

import com.bighomework.planeTicketWeb.enums.Gender;
import com.bighomework.planeTicketWeb.enums.MembershipLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerId;

    @Column(length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 18)
    private String idCard;

    @Column(length = 20, unique = true)
    private String phone;

    @Column(length = 50)
    private String email;

    @Enumerated(EnumType.STRING)
    private MembershipLevel membershipLevel;
    
    // 优化：移除长度限制，让其默认为 255
    @Column(nullable = false)
    private String password;

}