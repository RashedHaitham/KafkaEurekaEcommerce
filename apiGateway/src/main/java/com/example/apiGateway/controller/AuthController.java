package com.example.apiGateway.controller;

import com.example.apiGateway.service.UserService;
import com.example.apiGateway.util.JWTUtil;
import com.example.apiGateway.model.AuthRequest;
import com.example.apiGateway.model.AuthResponse;
import com.example.apiGateway.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@RestController
@RequestMapping("/api/v1") // Versioning the API
public class AuthController {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    // POST /api/v1/auth/login - User Login

    @PostMapping("/auth/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest authRequest, ServerHttpResponse response) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                authRequest.getUsername(), authRequest.getPassword()
        );
        Authentication authentication = authenticationManager.authenticate(authToken);

        return Mono.from(authentication.isAuthenticated()
                ? userService.findByUsername(authRequest.getUsername())
                .flatMap(user -> {
                    String accessToken = jwtUtil.generateToken(user.getUsername(), user.getFullName(), user.getEmail());

                    // Set JWT as a cookie
                    ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", accessToken)
                            .httpOnly(true)
                            //.secure(true)  // Enable in production
                            .path("/")  // Set path for the entire application
                            .maxAge(10 * 60 * 60)  // Token expiration (10 hours)
                            .build();

                    // Add cookie to the response
                    response.addCookie(jwtCookie);

                    return Mono.just(ResponseEntity.ok(new AuthResponse(accessToken)));
                })
                : Mono.just(ResponseEntity.badRequest().body(new AuthResponse("Invalid username or password"))));
    }


    // POST /api/v1/auth/signup - User Signup
    @PostMapping("/auth/signup")
    public Mono<ResponseEntity<String>> signup(@RequestBody User user) {
        return userService.savePerson(user)
                .map(savedUser -> ResponseEntity.status(HttpStatus.CREATED).body("User signed up successfully: " + savedUser.getFullName()))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error signing up: " + e.getMessage())));
    }

    // POST /api/v1/auth/refresh-token - Refresh JWT Token
    @PostMapping("/auth/refresh-token")
    public Mono<ResponseEntity<AuthResponse>> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);

        return userService.findByUsername(username)
                .flatMap(user -> {
                    String newAccessToken = jwtUtil.generateToken(user.getUsername(), user.getFullName(), user.getEmail());
                    return Mono.just(ResponseEntity.ok(new AuthResponse(newAccessToken)));
                })
                .onErrorResume(ex -> Mono.just(ResponseEntity.badRequest().body(new AuthResponse("Invalid refresh token"))));
    }

    // GET /api/v1/users - Get all users
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

    // POST /api/v1/users - Add a new user
    @PostMapping("/users")
    public Mono<ResponseEntity<String>> addUser(@RequestBody User user) {
        return userService.savePerson(user)
                .map(savedUser -> ResponseEntity.status(HttpStatus.CREATED).body("User added successfully: " + savedUser.getFullName()))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error adding user: " + e.getMessage())));
    }

    // DELETE /api/v1/users/{username} - Delete a user
    @DeleteMapping("/users/{username}")
    public Mono<ResponseEntity<String>> deleteUser(@PathVariable String username) {
        return userService.findByUsername(username)
                .flatMap(user -> userService.deletePerson(user)
                        .then(Mono.just(ResponseEntity.ok("User deleted successfully: " + username))))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found: " + username)))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage())));
    }

    @GetMapping("/protected")
    public Mono<ResponseEntity<String>> protectedEndpoint() {
        return Mono.just(ResponseEntity.ok("You have accessed a protected endpoint!"));
    }
}
