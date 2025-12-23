package com.bighomework.planeTicketWeb.dto.responseDTO;

import com.bighomework.planeTicketWeb.enums.MembershipLevel; // 【新增】导入会员等级枚举

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse implements Serializable {
    private String token;
    private String username; // 对应用户的手机号

    private Integer id;

    private String name;
    private MembershipLevel membershipLevel;
}