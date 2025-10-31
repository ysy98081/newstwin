package com.est.newstwin.controller.api;

import com.est.newstwin.domain.Member;
import com.est.newstwin.dto.api.ApiResponse;
import com.est.newstwin.dto.member.MemberRequestDto;
import com.est.newstwin.dto.member.MemberResponseDto;
import com.est.newstwin.exception.CustomException;
import com.est.newstwin.exception.ErrorCode;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    /**
     * 회원가입 처리
     * - 이메일 중복 검사 후 신규 회원 등록
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MemberResponseDto>> signup(
            @Valid @RequestBody MemberRequestDto requestDto) {

        MemberResponseDto response = memberService.signup(requestDto);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }

    /**
     * 현재 로그인한 사용자 정보 조회 (개발용 api)
     * - SecurityContextHolder에서 인증 정보 추출
     * - JWT 쿠키 인증을 기반으로 동작
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponseDto>> getCurrentMember() {

        // 현재 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // principal 에서 이메일(로그인 ID) 추출
        String email = authentication.getName();

        // 이메일로 Member 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 응답 DTO 변환
        MemberResponseDto response = MemberResponseDto.fromEntity(member);

        return ResponseEntity.ok(ApiResponse.success("로그인된 사용자 정보입니다.", response));
    }
}
