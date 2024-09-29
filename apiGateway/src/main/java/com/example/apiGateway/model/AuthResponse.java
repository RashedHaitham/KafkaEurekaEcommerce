package com.example.apiGateway.model;

import lombok.*;

@Setter
@Getter
@Data
@NoArgsConstructor  // Ensure there's a no-arg constructor for Jackson
@AllArgsConstructor
public class AuthResponse {
    private String token;
}
