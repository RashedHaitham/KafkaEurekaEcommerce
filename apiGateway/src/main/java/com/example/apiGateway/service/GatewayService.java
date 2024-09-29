package com.example.apiGateway.service;

import com.example.apiGateway.exception.UserNotFoundException;
import com.example.apiGateway.model.AuthRequest;
import com.example.apiGateway.model.AuthResponse;
import com.example.apiGateway.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.ResponseCookie;


@Service
public class GatewayService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    public Mono<ResponseEntity<AuthResponse>> login(AuthRequest authRequest) {
        String securityServiceUrl = "http://localhost:8088/security/authenticate";

        return webClientBuilder.build()
                .post()
                .uri(securityServiceUrl)
                .bodyValue(authRequest)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid username or password")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Error communicating with security service")))
                .bodyToMono(AuthResponse.class)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse("Error: " + e.getMessage()))));
    }

    public Mono<ResponseEntity<String>> signup(User user) {
        String securityServiceUrl = "http://localhost:8088/security/signup";

        return webClientBuilder.build()
                .post()
                .uri(securityServiceUrl)
                .bodyValue(user)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    // Log the response for better insight
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                System.out.println("Client error: " + errorBody);  // Log the error
                                return Mono.error(new RuntimeException("Error signing up: " + errorBody));
                            });
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                    // Log the server error for insight
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                System.out.println("Server error: " + errorBody);  // Log the error
                                return Mono.error(new RuntimeException("Error communicating with security service: " + errorBody));
                            });
                })
                .bodyToMono(String.class)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(e -> {
                    System.out.println("Error during signup: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Error signing up: " + e.getMessage()));
                });
    }



    public Mono<ResponseEntity<AuthResponse>> refreshToken(String refreshToken) {
        String securityServiceUrl = "http://localhost:8088/security/refresh-token";

        return webClientBuilder.build()
                .post()
                .uri(securityServiceUrl)
                .cookie("jwtToken", refreshToken)  // Send the existing token in the request as a cookie
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .map(authResponse -> {
                    String newToken = authResponse.getToken();

                    ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", newToken)
                            .httpOnly(true)
                            .path("/")
                            .maxAge(10 * 60 * 60)  // Token expiration (10 hours)
                            .build();

                    return ResponseEntity.ok()
                            .header("Set-Cookie", jwtCookie.toString())  // Set the new cookie in the response
                            .body(new AuthResponse(newToken));  // Optionally include the new token in the response body
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AuthResponse("Error refreshing token: " + e.getMessage()))));
    }



    public Mono<ResponseEntity<Flux<User>>> getAllUsers() {
        String securityServiceUrl = "http://localhost:8088/security/users";

        return webClientBuilder.build()
                .get()
                .uri(securityServiceUrl)
                .retrieve()
                .bodyToFlux(User.class)
                .collectList()
                .flatMap(users -> {
                    Flux<User> userFlux = Flux.fromIterable(users);
                    return Mono.just(ResponseEntity.ok(userFlux));
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()));
    }

    public Mono<ResponseEntity<String>> deleteUser(String username) {
        String securityServiceUrl = "http://localhost:8088/security/users/" + username;  // Replace with your security service URL

        return webClientBuilder.build()
                .delete()
                .uri(securityServiceUrl)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new UserNotFoundException("User not found: " + username)))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Server error occurred")))
                .bodyToMono(String.class)
                .map(responseBody -> ResponseEntity.ok("User deleted successfully: " + username))  // Handle success
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage())));  // Handle errors

    }
}
