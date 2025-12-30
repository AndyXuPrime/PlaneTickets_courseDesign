package com.bighomework.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "family_members")
public class FamilyMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer memberId;

    private Integer userId; // 关联主账号ID

    private String name;
    private String phone;

    @Column(length = 255)
    private String idCard; // 加密存储

    @Column(updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();
}