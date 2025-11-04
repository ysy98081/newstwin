package com.est.newstwin.controller.page;

import com.est.newstwin.dto.member.MemberResponseDto;
import com.est.newstwin.dto.mypage.BookmarkPostResponseDto;
import com.est.newstwin.service.BookmarkService;
import com.est.newstwin.service.CategoryService;
import com.est.newstwin.service.MemberService;
import com.est.newstwin.service.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MypagePageController {

    private final MypageService mypageService;
    private final CategoryService categoryService;
    private final BookmarkService bookmarkService;

    /**
     * 마이페이지 메인
     */
    @GetMapping("/mypage")
    public String mypageMain() {

        return "mypage/main";
    }

    /**
     * 회원 정보 수정 페이지
     */
    @GetMapping("/mypage/edit")
    public String editPage(Model model) {
        addMemberInfoToModel(model);

        return "mypage/edit";
    }

    /**
     * 뉴스레터 구독 관리 페이지
     */
    @GetMapping("/mypage/subscription")
    public String subscriptionPage(Model model) {
        addMemberInfoToModel(model);

        return "mypage/subscription";
    }

    /**
     * 북마크한 뉴스 페이지
     */
    @GetMapping("/mypage/bookmarks")
    public String bookmarksPage(Model model) {
        addMemberInfoToModel(model);

        return "mypage/bookmarks";
    }

    /**
     * 회원 탈퇴 페이지
     */
    @GetMapping("/mypage/withdraw")
    public String withdrawPage(Model model) {
        addMemberInfoToModel(model);
        return "mypage/withdraw";
    }

    /**
     * 공통 메서드
     * 로그인된 유저의 member 정보와 category 목록을 모델에 추가
     */
    private void addMemberInfoToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // 로그인된 유저 정보 가져오기
        MemberResponseDto member = mypageService.getMyInfo(email);

        List<BookmarkPostResponseDto> bookmarks = bookmarkService.getBookmarks(email);

        model.addAttribute("member", member);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("bookmarks", bookmarks);
    }
}
