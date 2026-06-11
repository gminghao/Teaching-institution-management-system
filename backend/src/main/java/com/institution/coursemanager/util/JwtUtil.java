package com.institution.coursemanager.util;

import com.institution.coursemanager.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(key)
                .compact();
    }

    public String parseUsername(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (RuntimeException ex) {
            throw new UnauthorizedException("登录令牌无效或已过期");
        }
    }

    public boolean validateToken(String token) {
        parseUsername(token);
        return true;
    }
}
