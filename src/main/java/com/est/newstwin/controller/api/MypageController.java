package com.est.newstwin.controller.api;

import com.est.newstwin.dto.api.ApiResponse;
import com.est.newstwin.dto.member.MemberResponseDto;
import com.est.newstwin.dto.member.MemberUpdateRequestDto;
import com.est.newstwin.service.MypageService;
import lombok.RequiredArgsConstructor;
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

    /** 내 정보 수정 (닉네임, 비밀번호, 프로필 이미지 포함) */
    @PostMapping(value = "/me", consumes = "multipart/form-data")
    public ApiResponse<MemberResponseDto> updateMyInfo(@ModelAttribute MemberUpdateRequestDto requestDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        MemberResponseDto response = mypageService.updateMyInfo(email, requestDto);
        return ApiResponse.success("내 정보 수정 성공", response);
    }

}
