package com.bighomework.admin.controller;

import com.bighomework.admin.entity.SystemMessage;
import com.bighomework.admin.service.SystemMessageService;
import com.bighomework.common.dto.requestDTO.MessageRequest;
import com.bighomework.common.enums.UserRole;
import com.bighomework.common.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/messages")
@RequiredArgsConstructor
public class MessageController {

    private final SystemMessageService messageService;

    /**
     * 1. 管理员发布消息
     * 权限要求: ROLE_PLATFORM_ADMIN
     */
    @PostMapping("/publish")
    public ApiResponse<Void> publish(
            @RequestBody MessageRequest req,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("收到发布消息请求: title={}, role={}", req.getTitle(), role);

        // 权限校验
        if (!UserRole.ROLE_PLATFORM_ADMIN.name().equals(role)) {
            return ApiResponse.error(403, "权限不足：仅平台管理员可发布全员广播");
        }

        // 调用 Service 发布
        messageService.publish(req.getTitle(), req.getContent(), "系统管理员");
        return ApiResponse.success(null, "发布成功");
    }

    /**
     * 2. 用户获取消息列表
     * 权限要求: 登录用户 (Gateway 已校验 Token，这里直接放行)
     */
    @GetMapping("/public")
    public ApiResponse<List<SystemMessage>> getPublicMessages() {
        // 直接返回所有公共消息
        return ApiResponse.success(messageService.getAllPublicMessages());
    }
}