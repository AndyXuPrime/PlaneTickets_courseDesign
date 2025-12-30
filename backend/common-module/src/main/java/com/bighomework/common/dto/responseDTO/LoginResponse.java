package com.bighomework.common.dto.responseDTO;

import com.bighomework.common.enums.MembershipLevel;
import com.bighomework.common.enums.UserRole;
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

    /**
     * JWT 令牌 (前端需存储在 localStorage，后续请求放入 Header)
     */
    private String token;

    /**
     * 用户唯一标识 (对应 user_id)
     */
    private Integer id;

    /**
     * 登录账号 (通常是手机号)
     */
    private String username;

    /**
     * 用户真实姓名 (用于显示，如 "张三")
     */
    private String name;

    /**
     * 会员等级 (普通/银卡/金卡/白金)
     */
    private MembershipLevel membershipLevel;

    /**
     * 用户角色 (ROLE_USER, ROLE_PLATFORM_ADMIN, ROLE_AIRLINE_ADMIN)
     * 前端根据此字段判断显示哪些菜单
     */
    private UserRole role;

    /**
     * 所属航司代码 (仅当 role 为 ROLE_AIRLINE_ADMIN 时有值，否则为 null)
     */
    private String airlineCode;
    private String avatarUrl;
}