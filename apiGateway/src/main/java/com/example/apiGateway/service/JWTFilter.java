package com.example.apiGateway.service;

import com.example.apiGateway.util.JWTUtil;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        // Allow login and signup without authentication
        if (path.equals("/api/auth/login") || path.equals("/api/auth/signup")) {
            return chain.filter(exchange);
        }

        return Mono.justOrEmpty(jwtCookieOpt)
                .map(HttpCookie::getValue)
                .flatMap(token -> {
                    String username = jwtUtil.extractUsername(token);
                    if (jwtUtil.validateToken(token, username)) {

                        // Extract the username from the path if the path matches the user profile pattern
                        if (path.matches("/profile/user/\\w+")) {
                            String pathUsername = path.split("/")[3]; // Extract username from path
                            if (!username.equals(pathUsername)) {
                                // If the username in the JWT does not match the one in the path, return 403 Forbidden
                                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                                return exchange.getResponse().setComplete();
                            }
                        }

                        List<String> roles = jwtUtil.extractRoles(token);
                        List<GrantedAuthority> authorities = roles.stream()
                                .map(SimpleGrantedAuthority::new)  // Convert each role to a GrantedAuthority
                                .collect(Collectors.toList());

                        Authentication auth =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);

                        SecurityContext securityContext = new SecurityContextImpl(auth);

                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
                    }

                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));  // If no JWT, proceed with the request
    }


    private Optional<HttpCookie> getJwtTokenFromRequest(ServerHttpRequest request) {
        return Optional.ofNullable(request.getCookies().getFirst("jwtToken"));
    }
}
