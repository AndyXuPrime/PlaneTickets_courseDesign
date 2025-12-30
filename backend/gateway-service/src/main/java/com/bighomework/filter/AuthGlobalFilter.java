package com.bighomework.filter;

import com.bighomework.common.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
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

    private static final String[] WHITE_LIST = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/flights/search",
            "/api/flights/all"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 【新增】1. 跨域预检请求(OPTIONS)直接放行，否则前端会报CORS错误
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        log.info("Gateway拦截请求: {}", path);

        // 2. 白名单放行
        for (String whitePath : WHITE_LIST) {
            if (path.startsWith(whitePath)) {
                return chain.filter(exchange);
            }
        }

        // 3. 获取 Token
        String token = null;
        String bearerToken = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7);
        }

        // 4. 校验 Token 是否存在
        if (token == null) {
            log.warn("请求未携带Token: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            // 5. 校验 Token 合法性
            if (jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromJWT(token);
                // 确保 common-module 里有这两个方法
                String role = jwtTokenProvider.getRoleFromJWT(token);
                String airlineCode = jwtTokenProvider.getAirlineCodeFromJWT(token);

                log.info("Token校验通过，用户: {}, 角色: {}, 航司: {}", username, role, airlineCode);

                // 6. 传递用户信息给下游
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

        // 7. 校验失败
        log.warn("Token无效或已过期: {}", path);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}