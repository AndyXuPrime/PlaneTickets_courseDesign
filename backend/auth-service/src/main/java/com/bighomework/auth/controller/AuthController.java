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
import com.bighomework.common.enums.UserRole;
import com.bighomework.common.enums.UserStatus;
import java.util.List;
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

    // ================= 已有跑通接口 (已根据新 Token 逻辑优化) =================

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        // 1. Spring Security 认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        // 2. 从数据库获取完整的用户信息（包含角色和航司代码）
        String phone = authentication.getName();
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("登录成功但无法在数据库中找到对应的用户信息"));

        // 3. 【核心优化】调用增强版的 generateToken，将角色和航司代码塞进 JWT
        String token = jwtTokenProvider.generateToken(
                user.getPhone(),
                user.getRole().name(),
                user.getAirlineCode()
        );

        // 4. 构建响应
        LoginResponse response = convertToLoginResponse(user, token);
        return ResponseEntity.ok(ApiResponse.success(response, "登录成功"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true), "注册成功"));
    }

    /**
     * 内部接口：供其他服务远程调用获取用户信息
     */
    @GetMapping("/internal/user")
    public ApiResponse<LoginResponse> getUserByPhone(@RequestParam("phone") String phone) {
        User user = userRepository.findByPhone(phone).orElse(null);
        if (user == null) return ApiResponse.success(null);
        return ApiResponse.success(convertToLoginResponse(user, null));
    }

    // ================= 新增管理接口 (支持后台管理) =================

    /**
     * 【平台管理员专用】获取所有待审核的航司管理员列表
     */
    @GetMapping("/admin/pending-list")
    public ApiResponse<List<User>> getPendingAdmins() {
        List<User> pendingUsers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ROLE_AIRLINE_ADMIN && u.getStatus() == UserStatus.PENDING)
                .collect(Collectors.toList());
        return ApiResponse.success(pendingUsers);
    }

    /**
     * 【平台管理员专用】审核航司管理员
     */
    @PutMapping("/admin/audit")
    public ApiResponse<Void> auditUser(@RequestParam Integer userId, @RequestParam UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("该申请记录不存在"));

        if (user.getRole() != UserRole.ROLE_AIRLINE_ADMIN) {
            throw new BusinessException("只能审核航司管理员申请");
        }

        user.setStatus(status);
        userRepository.save(user);
        log.info("管理员审核操作：用户ID={}, 结果={}", userId, status);
        return ApiResponse.success(null, "审核操作成功");
    }

    // --- 私有辅助方法 ---
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