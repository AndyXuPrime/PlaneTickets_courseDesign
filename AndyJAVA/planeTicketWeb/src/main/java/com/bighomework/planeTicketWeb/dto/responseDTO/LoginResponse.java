package com.bighomework.planeTicketWeb.dto.responseDTO;

import com.bighomework.planeTicketWeb.enums.MembershipLevel; // 【新增】导入会员等级枚举

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username; // 对应用户的手机号

    /**
     * 【核心新增】
     * 添加了 name 和 membershipLevel 字段，以便在登录成功后
     * 将更丰富的用户信息返回给前端，用于界面展示。
     */
    private String name;
    private MembershipLevel membershipLevel;
}