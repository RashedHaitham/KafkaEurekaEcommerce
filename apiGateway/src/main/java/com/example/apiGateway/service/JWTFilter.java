package com.example.apiGateway.service;
import com.example.apiGateway.util.JWTUtil;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Optional;

@Component
public class JWTFilter implements WebFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Optional<HttpCookie> jwtCookieOpt = getJwtTokenFromRequest(exchange.getRequest());

        String path = exchange.getRequest().getPath().toString();
        if (path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/signup")) {
            return chain.filter(exchange);
        }

        return Mono.justOrEmpty(jwtCookieOpt)
                .map(HttpCookie::getValue)
                .flatMap(token -> {
                    String username = jwtUtil.extractUsername(token);
                    if (jwtUtil.validateToken(token, username)) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());

                        SecurityContext securityContext = new SecurityContextImpl(auth);
                        System.out.println(securityContext.getAuthentication());
                        System.out.println(securityContext.getAuthentication().getCredentials());
                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
                    }

                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    private Optional<HttpCookie> getJwtTokenFromRequest(ServerHttpRequest request) {
        return Optional.ofNullable(request.getCookies().getFirst("jwtToken"));
    }
}
