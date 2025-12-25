package com.bighomework.common.enums;

/**
 * 用户账户状态
 */
public enum UserStatus {
    /**
     * 待审核 (航司管理员注册后默认为此状态)
     */
    PENDING,

    /**
     * 已激活 (正常使用)
     */
    ACTIVE,

    /**
     * 已拒绝/已禁用
     */
    REJECTED
}