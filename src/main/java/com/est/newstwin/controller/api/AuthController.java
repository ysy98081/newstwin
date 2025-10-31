package com.est.newstwin.controller.api;

import com.est.newstwin.dto.api.ApiResponse;
import com.est.newstwin.dto.auth.LoginRequestDto;
import com.est.newstwin.dto.auth.LoginResponseDto;
import com.est.newstwin.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인
     * - 사용자 인증 후 JWT 발급
     * - HttpOnly 쿠키에 Access Token 저장
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto requestDto,
            HttpServletResponse response) {

        // 로그인 처리 및 JWT 발급
        LoginResponseDto loginResponse = authService.login(requestDto);

        // HttpOnly 쿠키로 Access Token 전달
        ResponseCookie cookie = ResponseCookie.from("accessToken", loginResponse.getAccessToken())
                .httpOnly(true)       // JS에서 접근 불가 → XSS 방지
                .secure(false)        // HTTPS 환경이면 true로 변경
                .path("/")            // 모든 경로에서 접근 가능
                .maxAge(Duration.ofHours(1)) // 만료시간 설정
                .sameSite("Lax")      // 기본은 Lax
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 응답 바디에는 토큰을 포함하지 않고 (보안상 이유로) 클라이언트는 쿠키 기반으로 인증됨
        return ResponseEntity.ok(ApiResponse.success("로그인 성공", null));
    }

    /**
     * 로그아웃
     * - accessToken 쿠키 만료시켜 제거
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {

        // accessToken 쿠키를 만료시켜 클라이언트에서 삭제
        ResponseCookie expiredCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공", null));
    }
}
