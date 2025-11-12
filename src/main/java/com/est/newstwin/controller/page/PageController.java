package com.est.newstwin.controller.page;

import com.est.newstwin.exception.CustomException;
import com.est.newstwin.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final MemberService memberService;

    /* ---------- 홈 ---------- */
    @GetMapping("/")
    public String home() { return "index"; }

    /* ---------- 인증 (Auth) ---------- */
    @GetMapping("/login")
    public String loginPage() { return "auth/login"; }

    @GetMapping("/signup")
    public String signupPage() { return "auth/signup"; }

    /**
     * 이메일 인증 결과 페이지
     * - 토큰 검증 성공/실패에 따라 메시지/버튼 렌더링
     */
    @GetMapping("/verify")
    public String verifyEmailPage(@RequestParam("token") String token, Model model) {
        try {
            String message = memberService.verifyEmail(token); // 성공 시 isActive=true
            model.addAttribute("success", true);
            model.addAttribute("title", "이메일 인증 완료");
            model.addAttribute("message", message);
            model.addAttribute("redirectSeconds", 5); // 5초 후 로그인 이동
        } catch (CustomException e) {
            model.addAttribute("success", false);
            model.addAttribute("title", "이메일 인증 실패");
            model.addAttribute("message", "인증 링크가 만료되었거나 올바르지 않습니다.");
            model.addAttribute("redirectSeconds", 0);
        }
        return "auth/verify-result";
    }

    @GetMapping("/verify-info")
    public String verifyInfoPage() {
        return "auth/verify-info";
    }

    /* ---------- 관리자 (Admin) ---------- */
    @GetMapping("/admin/login")
    public String adminLogin() { return "admin/login"; }

    @GetMapping("/admin/posts-contents")
    public String adminPostsContents() { return "admin/posts-contents"; }

}
