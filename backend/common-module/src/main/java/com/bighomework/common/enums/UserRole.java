package com.bighomework.common.enums;

public enum UserRole {
    /**
     * 普通用户/乘客
     */
    ROLE_USER,

    /**
     * 平台管理员 (拥有最高权限，审核航司，管理基础数据)
     */
    ROLE_PLATFORM_ADMIN,

    /**
     * 航司管理员 (仅管理自己所属航司的航班和数据)
     */
    ROLE_AIRLINE_ADMIN
}