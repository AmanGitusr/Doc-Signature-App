package com.docsignature.backend.security;

import com.docsignature.backend.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtService {
    private final AppProperties properties;
    private final SecretKey secretKey;

    public JwtService(AppProperties properties) {
        this.properties = properties;
        byte[] keyBytes = properties.jwt().secret().getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes.length >= 32 ? keyBytes : pad(keyBytes));
    }

    public String generateToken(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.jwt().expirationMinutes(), ChronoUnit.MINUTES)))
                .signWith(secretKey)
                .compact();
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String subject) {
        try {
            Claims claims = parseClaims(token);
            return subject.equals(claims.getSubject()) && claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    private byte[] pad(byte[] keyBytes) {
        byte[] padded = new byte[32];
        for (int i = 0; i < padded.length; i++) {
            padded[i] = i < keyBytes.length ? keyBytes[i] : 0;
        }
        return padded;
    }
}
