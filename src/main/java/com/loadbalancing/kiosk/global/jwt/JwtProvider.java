package com.loadbalancing.kiosk.global.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration}") long expiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
    }

    //토큰 발급 (로그인 성공했을 때 호출)
    public String generateToken(String adminId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
            .subject(adminId)      // 토큰 주인 (관리자 id를 넣어둠)
            .issuedAt(now)         // 발급 시간
            .expiration(expiry)    // 뺏는 시간
            .signWith(secretKey)   // 비밀키 췍
            .compact();
    }

    // 토큰에서 adminId 꺼내기
    public String getAdminId(String token){
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    // 토큰이 유효한지 검사 (서명 위조/만료 여부)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false; // 만료됐거나 위조된 토큰이면 예외 터짐 → false로 처리
        }
    }
}
