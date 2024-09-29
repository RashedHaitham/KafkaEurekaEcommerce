//package com.example.apiGateway.service;
//
//import com.example.apiGateway.util.JWTUtil;
//import org.springframework.security.authentication.ReactiveAuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
//import org.springframework.stereotype.Component;
//import reactor.core.publisher.Mono;
//
//import java.util.ArrayList;
//
//@Component
//public class JWTAuthenticationManager implements ReactiveAuthenticationManager {
//
//    private final JWTUtil jwtUtil;
//    private final UserService userService;
//
//    public JWTAuthenticationManager(JWTUtil jwtUtil, UserService userService) {
//        this.jwtUtil = jwtUtil;
//        this.userService = userService;
//    }
//
//
//    @Override
//    public Mono<Authentication> authenticate(Authentication authentication) throws AuthenticationException {
//        String token = authentication.getCredentials().toString();
//        String username = jwtUtil.extractUsername(token);
//        System.out.println("Authenticating user with JWT...");
//
//        return userService.findByUsername(username)
//                .map(userDetails -> {
//                    if (jwtUtil.validateToken(token, userDetails.getUsername())) {
//                        return new UsernamePasswordAuthenticationToken(
//                                username, null, new ArrayList<>() // Provide roles/authorities as needed
//                        );
//                    } else {
//                        throw new AuthenticationException("Invalid JWT token") {};
//                    }
//                });
//    }
//
//    public ServerAuthenticationConverter authenticationConverter() {
//        return exchange -> {
//            String token = exchange.getRequest().getHeaders().getFirst("Authorization");
//            if (token != null && token.startsWith("Bearer ")) {
//                token = token.substring(7);
//                return Mono.just(new UsernamePasswordAuthenticationToken(token, token));
//            }
//            return Mono.empty();
//        };
//    }
//
//
//}
