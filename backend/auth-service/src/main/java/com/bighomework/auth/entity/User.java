package com.bighomework.auth.entity;

import com.bighomework.common.enums.Gender;
import com.bighomework.common.enums.MembershipLevel;
import com.bighomework.common.enums.UserRole;
import com.bighomework.common.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users") // 对应新的表名
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id") // 对应数据库 user_id
    private Integer userId;

    @Column(length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 255, name = "id_card")
    private String idCard;

    @Column(length = 20, unique = true)
    private String phone;

    @Column(length = 50)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_level")
    private MembershipLevel membershipLevel;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role; // 角色：ROLE_USER, ROLE_AIRLINE_ADMIN...

    @Column(name = "airline_code", length = 2)
    private String airlineCode; // 仅航司管理员有值

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status; // 状态：PENDING, ACTIVE...

    @Column(name = "avatar_url")
    private String avatarUrl;
}