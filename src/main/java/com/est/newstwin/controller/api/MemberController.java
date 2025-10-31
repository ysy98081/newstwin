package com.est.newstwin.controller.api;

import com.est.newstwin.dto.api.ApiResponse;
import com.est.newstwin.dto.member.MemberRequestDto;
import com.est.newstwin.dto.member.MemberResponseDto;
import com.est.newstwin.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MemberResponseDto>> signup(
            @Valid @RequestBody MemberRequestDto requestDto) {

        MemberResponseDto response = memberService.signup(requestDto);

        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }
}
