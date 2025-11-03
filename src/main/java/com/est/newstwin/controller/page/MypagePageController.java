package com.est.newstwin.controller.page;

import com.est.newstwin.dto.member.MemberResponseDto;
import com.est.newstwin.service.CategoryService;
import com.est.newstwin.service.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MypagePageController {

    private final MypageService mypageService;
    private final CategoryService categoryService;

    @GetMapping("/mypage")
    public String mypageMain() {
        return "mypage/main";
    }

    @GetMapping("/mypage/edit")
    public String editPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // 로그인된 유저 정보 가져오기
        MemberResponseDto member = mypageService.getMyInfo(email);

        model.addAttribute("member", member);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "mypage/edit";
    }

    @GetMapping("/mypage/subscription")
    public String subscriptionPage() {
        return "mypage/subscription";
    }

    @GetMapping("/mypage/bookmarks")
    public String bookmarksPage() {
        return "mypage/bookmarks";
    }

    @GetMapping("/mypage/withdraw")
    public String withdrawPage() {
        return "mypage/withdraw";
    }
}
