package com.bighomework.common.feign;

import com.bighomework.common.dto.responseDTO.LoginResponse; // 复用这个DTO或新建UserDTO
import com.bighomework.common.util.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service", path = "/api/auth")
public interface AuthFeignClient {
    @GetMapping("/internal/user")
    ApiResponse<LoginResponse> getUserByPhone(@RequestParam("phone") String phone);
}