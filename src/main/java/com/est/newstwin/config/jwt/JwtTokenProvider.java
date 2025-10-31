package com.est.newstwin.config.jwt;

import com.est.newstwin.domain.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증 유틸리티
 * - 사용자 정보를 기반으로 JWT Access Token 생성
 * - 토큰의 유효성 및 만료 여부 검증
 * - 토큰에서 사용자 이메일(subject) 추출
 */

@Component
public class JwtTokenProvider {

    private SecretKey key;
    private final String secretKey;
    private final long accessTokenValidTime;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-time}") long accessTokenValidTime
    ) {
        this.secretKey = secretKey;
        this.accessTokenValidTime = accessTokenValidTime;
    }

    @PostConstruct
    protected void init() {
        // Base64로 인코딩된 secretKey를 디코드하여 SecretKey로 변환
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    /**
     * Access Token 생성
     * - 이메일(subject), 역할(role), 발급일, 만료일을 포함
     */
    public String generateToken(Member member) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidTime);

        return Jwts.builder()
                .subject(member.getEmail())
                .claim("role", member.getRole().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * JWT 토큰 유효성 검증
     * - 서명 및 만료 여부 확인
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 토큰에서 이메일(subject) 추출
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }
}
