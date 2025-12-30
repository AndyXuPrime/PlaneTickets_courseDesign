package com.bighomework.auth.controller;

import com.bighomework.auth.entity.FamilyMember;
import com.bighomework.auth.entity.User;
import com.bighomework.auth.repository.UserRepository;
import com.bighomework.auth.service.AuthService;
import com.bighomework.common.dto.requestDTO.*;
import com.bighomework.common.dto.responseDTO.LoginResponse;
import com.bighomework.common.dto.responseDTO.FamilyMemberVO;
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

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        User user = userRepository.findByPhone(auth.getName()).orElseThrow(() -> new BusinessException("用户不存在"));
        String token = jwtTokenProvider.generateToken(user.getPhone(), user.getRole().name(), user.getAirlineCode());
        return ResponseEntity.ok(ApiResponse.success(convertToLoginResponse(user, token)));
    }

    @PostMapping("/register")
    public ApiResponse<Object> register(@Valid @RequestBody RegisterRequest req) {
        authService.registerUser(req);
        return ApiResponse.success(Map.of("success", true), "注册成功");
    }

    @PutMapping("/user/profile")
    public ApiResponse<Void> updateProfile(@RequestHeader("X-User-Name") String phone, @Valid @RequestBody UpdateProfileRequest req) {
        authService.updateProfile(phone, req);
        return ApiResponse.success(null, "资料已更新");
    }

    @PutMapping("/user/password")
    public ApiResponse<Void> updatePassword(@RequestHeader("X-User-Name") String phone, @Valid @RequestBody UpdatePasswordRequest req) {
        authService.updatePassword(phone, req);
        return ApiResponse.success(null, "密码已修改");
    }

    @GetMapping("/family")
    public ApiResponse<List<FamilyMemberVO>> getFamily(@RequestHeader("X-User-Name") String phone) {
        log.info("收到获取常用乘机人请求，手机号: {}", phone); // 加日志！

        if (phone == null) {
            return ApiResponse.error(401, "未登录或Token失效");
        }
        List<FamilyMember> members = authService.getFamilyMembers(phone);
        List<FamilyMemberVO> vos = members.stream()
                .map(m -> FamilyMemberVO.builder()
                        .memberId(m.getMemberId())
                        .name(m.getName())
                        .phone(m.getPhone())
                        .idCard(m.getIdCard())
                        .build())
                .collect(Collectors.toList());
        return ApiResponse.success(vos);
    }

    @PostMapping("/family")
    public ApiResponse<Void> addFamily(@RequestHeader("X-User-Name") String phone, @Valid @RequestBody FamilyMemberRequest req) {
        authService.addFamilyMember(phone, req);
        return ApiResponse.success(null, "添加成功");
    }

    @DeleteMapping("/family/{id}")
    public ApiResponse<Void> deleteFamily(@RequestHeader("X-User-Name") String phone, @PathVariable Integer id) {
        authService.deleteFamilyMember(id, phone);
        return ApiResponse.success(null, "删除成功");
    }

    @GetMapping("/admin/pending-list")
    public ApiResponse<List<User>> getPendingAdmins() {
        return ApiResponse.success(userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ROLE_AIRLINE_ADMIN && u.getStatus() == UserStatus.PENDING)
                .collect(Collectors.toList()));
    }

    @PutMapping("/admin/audit")
    @Transactional
    public ApiResponse<Void> auditUser(@RequestParam Integer userId, @RequestParam UserStatus status) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("记录不存在"));
        user.setStatus(status);
        userRepository.save(user);
        return ApiResponse.success(null, "审核完成");
    }

    @GetMapping("/internal/user")
    public ApiResponse<LoginResponse> getUserByPhone(@RequestParam("phone") String phone) {
        return userRepository.findByPhone(phone)
                .map(u -> ApiResponse.success(convertToLoginResponse(u, null)))
                .orElse(ApiResponse.success(null));
    }

    private LoginResponse convertToLoginResponse(User user, String token) {
        return LoginResponse.builder().token(token).id(user.getUserId()).username(user.getPhone())
                .name(user.getName()).membershipLevel(user.getMembershipLevel())
                .role(user.getRole()).airlineCode(user.getAirlineCode()).avatarUrl(user.getAvatarUrl()).build();
    }

    @GetMapping("/internal/family")
    public ApiResponse<List<FamilyMemberVO>> getFamilyInternal(@RequestParam("phone") String phone) {
        List<FamilyMember> members = authService.getFamilyMembers(phone);
        return ApiResponse.success(convertToVOList(members));
    }

    private List<FamilyMemberVO> convertToVOList(List<FamilyMember> members) {
        return members.stream()
                .map(m -> FamilyMemberVO.builder()
                        .memberId(m.getMemberId())
                        .name(m.getName())
                        .phone(m.getPhone())
                        .idCard(m.getIdCard())
                        .build())
                .collect(Collectors.toList());
    }



}