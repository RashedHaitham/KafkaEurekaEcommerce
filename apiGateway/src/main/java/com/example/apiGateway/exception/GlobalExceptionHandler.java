package com.example.apiGateway.exception;

import com.example.apiGateway.model.AuthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleException(Exception e) {
        return new ResponseEntity<>("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<String>> handleRuntimeException(RuntimeException ex) {
        return Mono.just(new ResponseEntity<>("Runtime error: " + ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<String>> handleUserNotFoundException(UserNotFoundException ex) {
        return Mono.just(new ResponseEntity<>("User not found: " + ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<AuthResponse>> handleBadRequest(ServerWebInputException e) {
        System.err.println("Bad Request Exception: " + e.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AuthResponse("Invalid input: " + e.getMessage())));
    }
}
