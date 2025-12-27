package com.bighomework.filter;

import com.bighomework.common.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // 白名单：不需要 Token 就能访问的接口
    private static final String[] WHITE_LIST = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/flights/search",
            "/api/flights/all"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.info("Gateway拦截请求: {}", path);

        // 1. 白名单放行
        for (String whitePath : WHITE_LIST) {
            if (path.startsWith(whitePath)) {
                return chain.filter(exchange);
            }
        }

        // 2. 获取 Token
        String token = null;
        String bearerToken = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7);
        }

        // 3. 校验 Token 是否存在
        if (token == null) {
            log.warn("请求未携带Token: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            // 4. 校验 Token 合法性
            if (jwtTokenProvider.validateToken(token)) {
                // 从 Token 中解析出用户信息
                String username = jwtTokenProvider.getUsernameFromJWT(token);
                // 【新增】解析角色和航司代码
                String role = jwtTokenProvider.getRoleFromJWT(token);
                String airlineCode = jwtTokenProvider.getAirlineCodeFromJWT(token);

                log.info("Token校验通过，用户: {}, 角色: {}, 航司: {}", username, role, airlineCode);

                // 5. 【关键】将用户信息封装进 Header 传递给下游微服务
                ServerHttpRequest request = exchange.getRequest().mutate()
                        .header("X-User-Name", username)
                        .header("X-User-Role", role != null ? role : "")
                        .header("X-Airline-Code", airlineCode != null ? airlineCode : "")
                        .build();

                return chain.filter(exchange.mutate().request(request).build());
            }
        } catch (Exception e) {
            log.error("Token校验异常: {}", e.getMessage());
        }

        // 6. 校验失败
        log.warn("Token无效或已过期: {}", path);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // 优先级最高
    }
}