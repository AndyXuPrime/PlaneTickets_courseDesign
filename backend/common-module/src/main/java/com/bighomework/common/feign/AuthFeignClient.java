package com.bighomework.common.feign;

import com.bighomework.common.dto.responseDTO.*;
import com.bighomework.common.util.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "auth-service")
public interface AuthFeignClient {

    @GetMapping("/api/auth/internal/user")
    ApiResponse<LoginResponse> getUserByPhone(@RequestParam("phone") String phone);

    @GetMapping("/internal/family")
    ApiResponse<List<FamilyMemberVO>> getFamily(@RequestParam("phone") String phone);}