package com.est.newstwin.controller.page;

import com.est.newstwin.dto.member.MemberResponseDto;
import com.est.newstwin.service.MemberService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class AdminController {
  private final MemberService memberService;

  @GetMapping("admin/users")
  public String getMemberList(Model model) {
    List<MemberResponseDto> members = memberService.getAllMembers();
    model.addAttribute("members", members);
    return "admin/users";
  }

  // 회원 상태 토글
  @PatchMapping("/admin/users/{memberId}/status")
  @ResponseBody
  public MemberResponseDto toggleMemberStatus(@PathVariable Long memberId) {
    return memberService.toggleMemberStatus(memberId);
  }

  // 구독 상태 토글
  @PatchMapping("/admin/users/{memberId}/subscription/{categoryId}")
  @ResponseBody
  public ResponseEntity<MemberResponseDto> toggleSubscription(
      @PathVariable Long memberId,
      @PathVariable Long categoryId) {
    MemberResponseDto dto = memberService.toggleSubscriptionStatus(memberId, categoryId);
    return ResponseEntity.ok(dto);
  }
}
