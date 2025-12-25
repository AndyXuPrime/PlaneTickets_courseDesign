package com.bighomework.filter;

import com.bighomework.common.security.JwtTokenProvider;
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

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // 白名单：不需要 Token 就能访问的接口
    private static final String[] WHITE_LIST = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/flights/search" // 假设搜索航班不需要登录
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 1.如果是白名单，直接放行
        for (String whitePath : WHITE_LIST) {
            if (path.startsWith(whitePath)) {
                return chain.filter(exchange);
            }
        }

        // 2. 获取 Authorization Header
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        // 3. 校验 Token
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                // 4. 解析用户信息
                String username = jwtTokenProvider.getUsernameFromJWT(token);

                // 5. 【关键】将用户信息放入 Header 传递给下游微服务
                // 这样下游服务（如 OrderService）就能知道是谁在操作了
                ServerHttpRequest request = exchange.getRequest().mutate()
                        .header("X-User-Name", username)
                        .build();
                return chain.filter(exchange.mutate().request(request).build());
            }
        }

        // 6. 校验失败，返回 401
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // 优先级，越小越先执行
    }
}