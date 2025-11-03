package com.est.newstwin.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    /* ---------- 홈 ---------- */
    @GetMapping("/")
    public String home() {
        return "index";  // templates/index.html
    }

    /* ---------- 인증 (Auth) ---------- */
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "auth/signup";
    }

/*
    */
/* ---------- 뉴스 (News) ---------- *//*

    @GetMapping("/news")
    public String newsList() {
        return "news/list";
    }

    @GetMapping("/news/detail")
    public String newsDetail() {
        return "news/detail";
    }
*/

    /* ---------- 마이페이지 (Mypage) ---------- */
    @GetMapping("/mypage")
    public String mypageMain() {
        return "mypage/main";
    }

    @GetMapping("/mypage/edit")
    public String mypageEdit() {
        return "mypage/edit";
    }

    @GetMapping("/mypage/subscription")
    public String mypageSubscription() {
        return "mypage/subscription";
    }

    @GetMapping("/mypage/bookmarks")
    public String mypageBookmarks() {
        return "mypage/bookmarks";
    }

    @GetMapping("/mypage/withdraw")
    public String mypageWithdraw() {
        return "mypage/withdraw";
    }

    /* ---------- 관리자 (Admin) ---------- */
    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login";
    }

    @GetMapping("/admin")
    public String adminDashboard() {
        return "admin/dashboard";
    }

//    @GetMapping("/admin/users")
//    public String adminUsers() {
//        return "admin/users";
//    }

    @GetMapping("/admin/posts")
    public String adminPosts() {
        return "admin/posts";
    }

    @GetMapping("/admin/posts-contents")
    public String adminPostsContents() { return "admin/posts-contents"; }

    @GetMapping("/admin/mails")
    public String adminMails() {
        return "admin/mails";
    }

    @GetMapping("/admin/mails-contents")
    public String adminMailsContents() {
      return "admin/mails-contents";
    }
}
