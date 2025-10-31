package com.est.newstwin.config.jwt;

import com.est.newstwin.domain.Member;
import com.est.newstwin.repository.MemberRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 인증 필터
 * - 요청마다 쿠키나 헤더에서 JWT 토큰을 추출하고 유효성 검증 수행
 * - 유효한 토큰이면 SecurityContext에 사용자 인증 정보 저장
 */

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // 요청에서 JWT 토큰 추출
        String token = extractToken(request);

        // 토큰이 존재하고 유효한 경우만 인증 처리
        if (token != null && jwtTokenProvider.validateToken(token)) {

            try {
                // 토큰에서 이메일 추출 후 사용자 조회
                String email = jwtTokenProvider.getEmailFromToken(token);
                Member member = memberRepository.findByEmail(email).orElse(null);

                if (member != null) {
                    // Spring Security 인증 객체 생성 및 컨텍스트 등록
                    User principal = new User(
                            member.getEmail(),
                            member.getPassword(),
                            Collections.emptyList()
                    );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException e) {
                // 유효하지 않은 토큰은 무시
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 헤더나 쿠키에서 JWT 토큰 추출
     * - Authorization 헤더 우선
     * - 없을 경우 accessToken 쿠키에서 검색
     */
    private String extractToken(HttpServletRequest request) {
        // Authorization 헤더 확인
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        // accessToken 쿠키 확인
        if (request.getCookies() != null) {

            for (Cookie cookie : request.getCookies()) {

                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
