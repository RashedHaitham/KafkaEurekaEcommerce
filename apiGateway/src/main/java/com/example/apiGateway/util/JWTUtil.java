package com.example.apiGateway.util;

import com.example.apiGateway.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

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
        final Claims claims = extractAllClaims(token);  // No more try-catch
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);  // No more try-catch
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("roles");
    }
}
