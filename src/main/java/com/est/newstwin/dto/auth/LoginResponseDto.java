package com.est.newstwin.dto.auth;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Builder;

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
