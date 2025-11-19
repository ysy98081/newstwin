package com.est.newstwin.controller.api;

import com.est.newstwin.dto.api.ApiResponse;
import com.est.newstwin.dto.member.MemberResponseDto;
import com.est.newstwin.dto.member.MemberUpdateRequestDto;
import com.est.newstwin.dto.mypage.SubscriptionRequestDto;
import com.est.newstwin.dto.mypage.SubscriptionResponseDto;
import com.est.newstwin.service.MypageService;
import com.est.newstwin.service.ProfileImageService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

/**
 * 마이페이지 관련 API 컨트롤러
 * - 회원 정보 조회 / 수정 / 탈퇴 / 구독 설정
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MypageController {

    private final MypageService mypageService;
    private final ProfileImageService profileImageService;

    /** 내 정보 조회 */
    @GetMapping("/me")
    public ApiResponse<MemberResponseDto> getMyInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        MemberResponseDto response = mypageService.getMyInfo(email);
        return ApiResponse.success("내 정보 조회 성공", response);
    }

    /** 프로필 임시등록 */
    @PostMapping(value="/profile/temp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadTempProfile(
       @RequestPart("file") MultipartFile file
    ) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        MemberResponseDto member = mypageService.getMyInfo(email);

        Long memberId = member.getId();

        // temp 업로드
        String tempUrl = profileImageService.uploadTemp(file, memberId);

        return ApiResponse.success("임시 업로드 성공", tempUrl);
  }

    /** 내 정보 수정 */
    @PostMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MemberResponseDto> updateMyInfo(
        @RequestPart(value = "memberName", required = false) String memberName,
        @RequestPart(value = "password", required = false) String password,
        @RequestPart(value = "receiveEmail", required = false) String receiveEmail,
        @RequestPart(value = "tempImageUrl", required = false) String tempImageUrl,
        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
      Boolean receive = receiveEmail != null ? Boolean.parseBoolean(receiveEmail) : null;

      MemberUpdateRequestDto dto = new MemberUpdateRequestDto(
          memberName,
          password,
          receive,
          profileImage,
          tempImageUrl
      );

      String email = SecurityContextHolder.getContext().getAuthentication().getName();
      return ApiResponse.success("내 정보 수정 성공", mypageService.updateMyInfo(email, dto));
    }

    /** 구독 목록 조회 */
    @GetMapping("/subscription")
    public ApiResponse<SubscriptionResponseDto> getSubscriptions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return ApiResponse.success("구독 목록 조회 성공", mypageService.getSubscriptions(email));
    }

    /** 구독 설정 저장 */
    @PostMapping("/subscription")
    public ApiResponse<Void> updateSubscriptions(@RequestBody SubscriptionRequestDto request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        mypageService.updateSubscriptions(email, request);
        return ApiResponse.success("구독 정보가 업데이트되었습니다.", null);
    }

    /** 회원 탈퇴 (비활성화 + JWT 쿠키 만료) */
    @PostMapping("/withdraw")
    public ApiResponse<Void> withdrawMember(HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // DB상에서 비활성화 처리
        mypageService.deactivateMember(email);

        // JWT 쿠키 즉시 만료
        ResponseCookie expiredCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

        return ApiResponse.success("회원 탈퇴(비활성화) 및 로그아웃 처리 완료", null);
    }
}
