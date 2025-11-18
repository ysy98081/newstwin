package com.est.newstwin.controller.api;

import com.est.newstwin.domain.Member;
import com.est.newstwin.dto.api.ApiResponse;
import com.est.newstwin.dto.member.EmailVerificationResponseDto;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 관련 API 컨트롤러
 * - 회원가입, 이메일 인증, 로그인 사용자 정보, 이메일 중복 확인
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MemberResponseDto>> signup(@Valid @RequestBody MemberRequestDto requestDto) {
        MemberResponseDto response = memberService.signup(requestDto);

        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다. 이메일 인증을 진행해주세요.", response));
    }

    /**
     * 이메일 인증
     */
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<EmailVerificationResponseDto>> verifyEmail(@RequestParam("token") String token) {
        String message = memberService.verifyEmail(token);
        EmailVerificationResponseDto dto = new EmailVerificationResponseDto(true, message);

        return ResponseEntity.ok(ApiResponse.success("이메일 인증이 완료되었습니다.", dto));
    }

    /**
     * 인증 메일 재발송
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@RequestParam("email") String email) {
        memberService.resendVerificationEmail(email);

        return ResponseEntity.ok(ApiResponse.success("인증 메일이 재발송되었습니다.", null));
    }

    /**
     * 현재 로그인한 사용자 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponseDto>> getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        String email = authentication.getName();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        MemberResponseDto response = MemberResponseDto.fromEntity(member);

        return ResponseEntity.ok(ApiResponse.success("로그인된 사용자 정보입니다.", response));
    }

    /**
     * 이메일 중복 확인
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Void>> checkEmail(@RequestParam String email) {
        boolean exists = memberRepository.findByEmail(email).isPresent();

        if (exists) return ResponseEntity.ok(ApiResponse.fail("이미 사용 중인 이메일입니다."));

        return ResponseEntity.ok(ApiResponse.success("사용 가능한 이메일입니다.", null));
    }

    /**
     * 이메일 존재 여부 확인 (뉴스레터 구독용)
     */
    @GetMapping("/exists")
    public ApiResponse<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = memberRepository.findByEmail(email).isPresent();

        return ApiResponse.success("이메일 존재 여부 확인", exists);
    }

    @Transactional
    @GetMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribe(@RequestParam Long memberId) {
      Member member = memberRepository.findById(memberId)
          .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

      if (!member.getReceiveEmail()) {
        return ResponseEntity.ok()
            .header("Content-Type", "text/html; charset=UTF-8")
            .body("""
                          <div style='font-family:sans-serif;padding:20px;'>
                          ✅ 이미 뉴스레터 수신이 해제된 상태입니다.<br><br>
                          <a href='https://newstwin.kro.kr/'>홈으로 돌아가기</a>
                          </div>
                          """);
      }

      member.setReceiveEmail(false);
      memberRepository.save(member);

      return ResponseEntity.ok()
          .header("Content-Type", "text/html; charset=UTF-8")
          .body("""
                      <div style='font-family:sans-serif;padding:20px;'>
                      ✅ 뉴스레터 수신이 해제되었습니다.<br>
                      앞으로 더 이상 메일이 발송되지 않습니다.<br><br>
                      <a href='https://newstwin.kro.kr/'>홈으로 돌아가기</a>
                      </div>
                      """);
    }
}
