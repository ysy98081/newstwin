package com.est.newstwin.service;

import com.est.newstwin.domain.Member;
import com.est.newstwin.dto.auth.LoginRequestDto;
import com.est.newstwin.dto.auth.LoginResponseDto;
import com.est.newstwin.exception.CustomException;
import com.est.newstwin.exception.ErrorCode;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스
 * - 사용자 로그인 처리 및 JWT 토큰 발급
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 로그인 후 JWT 토큰 발급
     */
    public LoginResponseDto login(LoginRequestDto requestDto) {

        // 이메일로 사용자 조회
        Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // JWT 토큰 발급
        String accessToken = jwtTokenProvider.generateToken(member);

        // 응답 생성
        return LoginResponseDto.of(accessToken, null, member.getMemberName());
    }
}
