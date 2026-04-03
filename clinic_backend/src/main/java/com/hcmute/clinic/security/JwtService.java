package com.hcmute.clinic.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Dịch vụ xử lý JSON Web Token (JWT).
 * Cung cấp các phương thức tạo mã thông báo cho đăng nhập và mã QR Check-in.
 */
@Service
public class JwtService {

    private static final long QR_TOKEN_EXPIRATION_MS = 3 * 60 * 1000; // 3 minutes

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        if (secret.length() < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 characters");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateQrToken(String patientId) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(patientId)
                .claim("purpose", "QR_CHECKIN")
                .claim("jti", UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + QR_TOKEN_EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(String subjectUserId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(subjectUserId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs)) // Access Token
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String subjectUserId, String role) {
        Date now = new Date();
        long refreshExpirationMs = 7L * 24 * 60 * 60 * 1000; // 7 days
        return Jwts.builder()
                .setSubject(subjectUserId)
                .claim("role", role)
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
