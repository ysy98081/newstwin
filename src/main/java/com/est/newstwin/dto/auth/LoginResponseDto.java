package com.est.newstwin.dto.auth;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 로그인 응답 DTO
 * - JWT Access Token 및 사용자 정보를 반환
 */


@Getter
@AllArgsConstructor
@Builder
public class LoginResponseDto {

    private String accessToken;     // JWT Access Token
    private String refreshToken;    // JWT Refresh Token (선택)
    private String tokenType;       // 예: "Bearer"
    private String memberName;      // 사용자 이름

    public static LoginResponseDto of(String accessToken, String refreshToken, String memberName) {
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .memberName(memberName)
                .build();
    }
}
