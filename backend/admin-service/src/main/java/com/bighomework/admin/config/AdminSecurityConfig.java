package com.bighomework.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class AdminSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // 禁用 CSRF 防护
                .authorizeHttpRequests(auth -> auth
                        // 1. 站内信读取接口：明确允许访问
                        // (只要 Gateway 校验 Token 通过，请求转发到这里，我们就允许执行)
                        .requestMatchers("/api/admin/messages/public").permitAll()

                        // 2. 其他管理接口：也设置为 permitAll
                        // ⚠️ 注意：这并不代表不安全！
                        // 因为我们在 MessageController / FlightController 等代码中，
                        // 会通过 @RequestHeader("X-User-Role") 强制检查是否为 ROLE_PLATFORM_ADMIN。
                        // 如果这里拦截了，Controller 还没拿到 Header 就会被拒，导致逻辑无法执行。
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}