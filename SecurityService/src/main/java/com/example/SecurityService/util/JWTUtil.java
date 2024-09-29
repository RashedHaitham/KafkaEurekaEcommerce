package com.example.SecurityService.util;

import com.example.SecurityService.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JWTUtil {

    private String secret = "GAzmNPg574X3riPuD5/zGcU1+QShdbOEvTHPgr2PMOs=";  // Load secret from environment variable

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);  // Decode the base64-encoded secret
        return Keys.hmacShaKeyFor(keyBytes);  // Recreate the key
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);  // No try-catch block
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Mono<String> generateToken(Mono<User> userMono, Collection<? extends GrantedAuthority> authorities) {
        return userMono.map(user -> {
            Map<String, Object> claims = new HashMap<>();

            // Add user information to the claims
            claims.put("fullname", user.getFullName());
            claims.put("email", user.getEmail());

            // Extract roles from authorities and add them as claims
            List<String> roles = authorities.stream()
                    .map(GrantedAuthority::getAuthority)  // Extract the role name
                    .collect(Collectors.toList());

            claims.put("roles", roles);  // Add the list of roles to the claims

            // Return the generated token with the claims
            return createToken(claims, user.getUsername());
        });
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))  // Set expiration (10 hours)
                .signWith(getSigningKey())  // Sign the token with the secret key
                .compact();  // Generate the token
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);  // No try-catch block
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("roles");  // Assuming roles are stored in the "roles" claim
    }
}
