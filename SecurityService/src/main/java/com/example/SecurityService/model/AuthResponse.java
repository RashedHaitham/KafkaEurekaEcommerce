package com.example.SecurityService.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class AuthResponse {

    private String token;

    public AuthResponse(String token) {
        this.token = token;
    }

}
