package com.est.newstwin.controller.api;

import com.est.newstwin.dto.api.ApiResponse;
import com.est.newstwin.dto.member.MemberResponseDto;
import com.est.newstwin.dto.member.MemberUpdateRequestDto;
import com.est.newstwin.dto.mypage.SubscriptionRequestDto;
import com.est.newstwin.dto.mypage.SubscriptionResponseDto;
import com.est.newstwin.service.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 마이페이지 관련 API 컨트롤러
 * - 회원 정보 조회 및 수정
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MypageController {

    private final MypageService mypageService;

    /** 내 정보 조회 */
    @GetMapping("/me")
    public ApiResponse<MemberResponseDto> getMyInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        MemberResponseDto response = mypageService.getMyInfo(email);
        return ApiResponse.success("내 정보 조회 성공", response);
    }

    /** 내 정보 수정 (필요한 항목만 수정 가능) */
    @PostMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MemberResponseDto> updateMyInfo(
            @RequestPart(value = "memberName", required = false) String memberName,
            @RequestPart(value = "password", required = false) String password,
            @RequestPart(value = "receiveEmail", required = false) String receiveEmail,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        Boolean receive = (receiveEmail != null) ? Boolean.parseBoolean(receiveEmail) : null;
        MemberUpdateRequestDto dto = new MemberUpdateRequestDto(memberName, password, receive, profileImage);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        MemberResponseDto response = mypageService.updateMyInfo(email, dto);
        return ApiResponse.success("내 정보 수정 성공", response);
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
}
