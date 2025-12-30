package com.bighomework.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 自动加盐，非常安全
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // =========================================================
                        // 1. 【最高优先级】放行内部接口 (供 Order/Flight 服务 Feign 调用)
                        //    必须放在最前面，确保服务间调用畅通无阻
                        // =========================================================
                        .requestMatchers("/api/auth/internal/**").permitAll()

                        // 2. 放行登录注册 (无需 Token)
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()

                        // 3. 【业务接口放行】
                        //    因为 Gateway 已经校验过 Token 并透传了用户信息 (X-User-Name)
                        //    Auth 服务本身不再重复校验 Token，直接放行，依靠 Controller 层读取 Header
                        .requestMatchers(
                                "/api/auth/family/**",  // 常用乘机人
                                "/api/auth/user/**",    // 用户资料修改
                                "/api/auth/admin/**"    // 管理员审核
                        ).permitAll()

                        // 4. 兜底策略 (保持 authenticated 以防万一有漏网之鱼)
                        .anyRequest().authenticated()
                );
        return http.build();
    }

}