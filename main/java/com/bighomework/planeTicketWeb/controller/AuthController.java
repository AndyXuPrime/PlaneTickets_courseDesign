package com.bighomework.planeTicketWeb.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bighomework.planeTicketWeb.dto.requestDTO.LoginRequest;
import com.bighomework.planeTicketWeb.dto.requestDTO.RegisterRequest;
import com.bighomework.planeTicketWeb.dto.responseDTO.LoginResponse;
import com.bighomework.planeTicketWeb.entity.Customer;
import com.bighomework.planeTicketWeb.exception.BusinessException;
import com.bighomework.planeTicketWeb.repository.CustomerRepository;
import com.bighomework.planeTicketWeb.security.JwtTokenProvider;
import com.bighomework.planeTicketWeb.service.AuthService;
import com.bighomework.planeTicketWeb.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final CustomerRepository customerRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String username = ((User) authentication.getPrincipal()).getUsername();
        Customer customer = customerRepository.findByPhone(username)
                .orElseThrow(() -> new BusinessException("登录成功但无法在数据库中找到对应的用户信息"));

        String jwt = jwtTokenProvider.generateToken(authentication);
        
      
        LoginResponse loginResponse = LoginResponse.builder()
                .token(jwt)
                .username(customer.getName())
                .id(customer.getCustomerId())
                .name(customer.getName())
                .membershipLevel(customer.getMembershipLevel())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "登录成功"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true), "注册成功"));
    }
}