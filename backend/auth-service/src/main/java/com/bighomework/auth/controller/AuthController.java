package com.bighomework.auth.controller;

import com.bighomework.auth.entity.User;
import com.bighomework.auth.repository.UserRepository;
import com.bighomework.auth.service.AuthService;
import com.bighomework.common.dto.requestDTO.LoginRequest;
import com.bighomework.common.dto.requestDTO.RegisterRequest;
import com.bighomework.common.dto.responseDTO.LoginResponse;
import com.bighomework.common.security.JwtTokenProvider;
import com.bighomework.common.util.ApiResponse;
import com.bighomework.common.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        // 1. 认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        // 2. 获取用户信息
        String phone = authentication.getName();
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 3. 生成 Token
        String token = jwtTokenProvider.generateToken(authentication);

        // 4. 构建响应 (使用提取的方法)
        LoginResponse response = convertToLoginResponse(user, token);

        return ResponseEntity.ok(ApiResponse.success(response, "登录成功"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true), "注册成功"));
    }

    /**
     * 内部接口：供 Order-Service 远程调用
     * 根据手机号获取用户信息
     */
    @GetMapping("/internal/user")
    public ApiResponse<LoginResponse> getUserByPhone(@RequestParam("phone") String phone) {
        User user = userRepository.findByPhone(phone).orElse(null);

        if (user == null) {
            // 返回 null data，调用方(OrderService)会处理
            return ApiResponse.success(null);
        }

        // 构建响应 (Token 传 null 即可，内部调用不需要返回 Token)
        LoginResponse response = convertToLoginResponse(user, null);

        return ApiResponse.success(response);
    }

    // --- 私有辅助方法 ---

    private LoginResponse convertToLoginResponse(User user, String token) {
        return LoginResponse.builder()
                .token(token)
                .id(user.getUserId())
                .username(user.getPhone()) // 修正：username 对应登录账号(手机号)
                .name(user.getName())      // name 对应真实姓名
                .membershipLevel(user.getMembershipLevel())
                .role(user.getRole())           // 确保 DTO 已包含此字段
                .airlineCode(user.getAirlineCode()) // 确保 DTO 已包含此字段
                .build();
    }
}