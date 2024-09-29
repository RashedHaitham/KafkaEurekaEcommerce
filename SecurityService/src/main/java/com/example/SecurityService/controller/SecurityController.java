package com.example.SecurityService.controller;

import com.example.SecurityService.model.AuthRequest;
import com.example.SecurityService.model.AuthResponse;
import com.example.SecurityService.model.User;
import com.example.SecurityService.service.UserService;
import com.example.SecurityService.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/security")
public class SecurityController {


    @Autowired
    private ReactiveAuthenticationManager reactiveAuthenticationManager;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/authenticate")
    public Mono<ResponseEntity<AuthResponse>> authenticate(@RequestBody AuthRequest authRequest) {
        Authentication authToken =
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword());

        return reactiveAuthenticationManager.authenticate(authToken)
                .flatMap(authentication -> userService.findByUsername(authRequest.getUsername())
                        .flatMap(user -> {
                            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                            return jwtUtil.generateToken(Mono.just(user), authorities)
                                    .map(token -> ResponseEntity.ok(new AuthResponse(token)));
                        }))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse("Invalid username or password"))));
    }


    @PostMapping("/signup")
    public Mono<ResponseEntity<String>> signup(@RequestBody User user) {
        return userService.savePerson(user)
                .flatMap(savedUser -> Mono.just(ResponseEntity.status(HttpStatus.CREATED)
                        .body("User signed up successfully: " + savedUser.getFullName())))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error signing up: " + e.getMessage())));
    }

    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<AuthResponse>> refreshToken(@CookieValue("jwtToken") String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);

        return userService.findByUsername(username)
                .flatMap(user -> {
                    Collection<? extends GrantedAuthority> authorities = jwtUtil.extractRoles(refreshToken)
                            .stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());  // Convert roles (List<String>) to GrantedAuthority
                    return jwtUtil.generateToken(Mono.just(user), authorities)
                            .map(token -> ResponseEntity.ok()
                                    .body(new AuthResponse(token)));
                })
                .onErrorResume(ex -> Mono.just(ResponseEntity.badRequest()
                        .body(new AuthResponse("Invalid refresh token"))));
    }


    @GetMapping("/users")
    public Mono<ResponseEntity<Flux<User>>> getAllUsers() {
        return userService.findAll()
                .hasElements()
                .flatMap(hasElements -> {
                    if (hasElements) {
                        return Mono.just(ResponseEntity.ok(userService.findAll()));
                    } else {
                        return Mono.just(ResponseEntity.noContent().build());
                    }
                });
    }

    @DeleteMapping("/users/{username}")
    public Mono<ResponseEntity<String>> deleteUser(@PathVariable String username) {
        return userService.findByUsername(username)
                .flatMap(user -> userService.deletePerson(user)
                        .then(Mono.just(ResponseEntity.ok("User deleted successfully: " + username))))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found: " + username)))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(e.getMessage())));
    }
}
