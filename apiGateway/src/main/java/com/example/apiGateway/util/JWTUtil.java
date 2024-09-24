package com.example.apiGateway.util;

import com.example.apiGateway.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JWTUtil {

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Creates a secure key
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);

        } catch (SignatureException e) {
            // Invalid signature detected
            System.out.println("Invalid JWT signature: " + e.getMessage());
            return null;

        } catch (ExpiredJwtException e) {
            // Token has expired
            System.out.println("JWT token has expired: " + e.getMessage());
            return null;

        } catch (MalformedJwtException e) {
            // Malformed token
            System.out.println("Invalid JWT token: " + e.getMessage());
            return null;

        } catch (IllegalArgumentException e) {
            // Empty or null JWT
            System.out.println("JWT claims string is empty: " + e.getMessage());
            return null;

        } catch (Exception e) {
            // General exception
            System.out.println("Error extracting claims from JWT: " + e.getMessage());
            return null;
        }
    }


    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Modify this method to accept both User and Authorities (roles)
    public String generateToken(User user, Collection<? extends GrantedAuthority> authorities) {
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
    }


    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))  // Set expiration (10 hours)
                .signWith(key)
                .compact();
    }


    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));

        } catch (SignatureException e) {
            System.out.println("Invalid JWT signature: " + e.getMessage());
            return false;

        } catch (ExpiredJwtException e) {
            System.out.println("JWT token has expired: " + e.getMessage());
            return false;

        } catch (MalformedJwtException e) {
            System.out.println("Invalid JWT token: " + e.getMessage());
            return false;

        } catch (IllegalArgumentException e) {
            System.out.println("JWT claims string is empty: " + e.getMessage());
            return false;

        } catch (Exception e) {
            System.out.println("JWT token validation error: " + e.getMessage());
            return false;
        }
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("roles");  // Assuming roles are stored in the "roles" claim
    }



}
