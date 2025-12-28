package com.bighomework.auth.controller;

import com.bighomework.auth.entity.User;
import com.bighomework.auth.repository.UserRepository;
import com.bighomework.auth.service.AuthService;
import com.bighomework.common.dto.requestDTO.LoginRequest;
import com.bighomework.common.dto.requestDTO.RegisterRequest;
import com.bighomework.common.dto.requestDTO.UpdatePasswordRequest;
import com.bighomework.common.dto.requestDTO.UpdateProfileRequest;
import com.bighomework.common.dto.responseDTO.LoginResponse;
import com.bighomework.common.enums.UserRole;
import com.bighomework.common.enums.UserStatus;
import com.bighomework.common.exception.BusinessException;
import com.bighomework.common.security.JwtTokenProvider;
import com.bighomework.common.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 1. 正确注入加密器

    // --- 登录与注册 ---

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        User user = userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new BusinessException("用户信息丢失"));

        String token = jwtTokenProvider.generateToken(user.getPhone(), user.getRole().name(), user.getAirlineCode());

        return ResponseEntity.ok(ApiResponse.success(convertToLoginResponse(user, token), "登录成功"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true), "注册成功"));
    }

    // --- 用户中心功能 ---

    @PutMapping("/user/profile")
    @Transactional
    public ApiResponse<Void> updateProfile(
            @RequestHeader("X-User-Name") String phone,
            @Valid @RequestBody UpdateProfileRequest req) {
        User user = userRepository.findByPhone(phone).orElseThrow(() -> new BusinessException("用户不存在"));
        user.setEmail(req.getEmail());
        user.setAvatarUrl(req.getAvatarUrl());
        userRepository.save(user);
        return ApiResponse.success(null, "资料更新成功");
    }

    @PutMapping("/user/password")
    @Transactional
    public ApiResponse<Void> updatePassword(
            @RequestHeader("X-User-Name") String phone,
            @Valid @RequestBody UpdatePasswordRequest req) {
        User user = userRepository.findByPhone(phone).orElseThrow(() -> new BusinessException("用户不存在"));

        // 2. 使用注入的 passwordEncoder 进行校验
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return ApiResponse.success(null, "密码修改成功");
    }

    // --- 后台管理功能 ---

    @GetMapping("/admin/pending-list")
    public ApiResponse<List<User>> getPendingAdmins() {
        List<User> pendingUsers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ROLE_AIRLINE_ADMIN && u.getStatus() == UserStatus.PENDING)
                .collect(Collectors.toList());
        return ApiResponse.success(pendingUsers);
    }

    @PutMapping("/admin/audit")
    @Transactional
    public ApiResponse<Void> auditUser(
            @RequestParam Integer userId,
            @RequestParam UserStatus status,
            @RequestParam(required = false) String reason) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("记录不存在"));
        user.setStatus(status);
        userRepository.save(user);
        log.info("审核操作：ID={}, 结果={}, 原因={}", userId, status, reason);
        return ApiResponse.success(null, "审核已完成");
    }

    // --- 内部调用与辅助 ---

    @GetMapping("/internal/user")
    public ApiResponse<LoginResponse> getUserByPhone(@RequestParam("phone") String phone) {
        return userRepository.findByPhone(phone)
                .map(user -> ApiResponse.success(convertToLoginResponse(user, null)))
                .orElse(ApiResponse.success(null));
    }

    private LoginResponse convertToLoginResponse(User user, String token) {
        return LoginResponse.builder()
                .token(token)
                .id(user.getUserId())
                .username(user.getPhone())
                .name(user.getName())
                .membershipLevel(user.getMembershipLevel())
                .role(user.getRole())
                .airlineCode(user.getAirlineCode())
                .build();
    }
}