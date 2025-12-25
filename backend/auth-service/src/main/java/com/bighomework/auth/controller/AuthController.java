package com.bighomework.auth.controller;

import com.bighomework.auth.entity.User;
import com.bighomework.auth.repository.UserRepository;
import com.bighomework.auth.service.AuthService;
import com.bighomework.common.dto.requestDTO.LoginRequest;
import com.bighomework.common.dto.requestDTO.RegisterRequest;
import com.bighomework.common.dto.responseDTO.LoginResponse;
import com.bighomework.common.security.JwtTokenProvider; // Common
import com.bighomework.common.util.ApiResponse;
import com.bighomework.common.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
        // 1. 认证 (这会调用 UserDetailsServiceImpl)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        // 2. 获取用户信息
        String phone = authentication.getName();
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 3. 生成 Token
        String token = jwtTokenProvider.generateToken(authentication);

        // 4. 构建响应 (包含角色和航司代码)
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .id(user.getUserId())
                .username(user.getName()) // 这里的 username 对应前端显示的姓名
                .name(user.getName())
                .membershipLevel(user.getMembershipLevel())
                // 确保 LoginResponse DTO 里加了这两个字段
                // .role(user.getRole())
                // .airlineCode(user.getAirlineCode())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "登录成功"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true), "注册成功"));
    }
}