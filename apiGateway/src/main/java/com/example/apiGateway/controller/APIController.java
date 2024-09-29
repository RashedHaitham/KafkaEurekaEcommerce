package com.example.apiGateway.controller;

import com.example.apiGateway.model.AuthRequest;
import com.example.apiGateway.model.AuthResponse;
import com.example.apiGateway.model.User;
import com.example.apiGateway.service.GatewayService;
import com.example.apiGateway.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@RestController
@RequestMapping("/api")
public class APIController {

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private JWTUtil jwtUtil;

    // POST /api/auth/login - User Login
    @PostMapping("/auth/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest authRequest, ServerHttpResponse response) {
        return gatewayService.login(authRequest)
                .flatMap(authResponse -> {
                    if (authResponse.getBody() != null) {
                        String token = authResponse.getBody().getToken();
                        ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", token)
                                .httpOnly(true)
                                .path("/")
                                .maxAge(10 * 60 * 60)  // Token expiration (10 hours)
                                .build();

                        response.addCookie(jwtCookie);

                        System.out.println(jwtUtil.extractRoles(token));
                    }
                    return Mono.just(authResponse);
                });
    }

    // POST /api/auth/signup - User Signup
    @PostMapping("/auth/signup")
    public Mono<ResponseEntity<String>> signup(@RequestBody User user) {
        return gatewayService.signup(user);
    }

    // POST /api/refresh-token - Refresh JWT Token
    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<AuthResponse>> refreshToken(@CookieValue("jwtToken") String refreshToken) {
        return gatewayService.refreshToken(refreshToken);
    }

    // GET /api/users - Get all users
    @GetMapping("/users")
    public Mono<ResponseEntity<Flux<User>>> getAllUsers() {
        return gatewayService.getAllUsers();
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @DeleteMapping("/users/{username}")
    public Mono<ResponseEntity<String>> deleteUser(@PathVariable String username) {
        return gatewayService.deleteUser(username);
        }

}
