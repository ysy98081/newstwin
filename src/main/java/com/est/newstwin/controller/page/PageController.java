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


    /* ---------- 관리자 (Admin) ---------- */
    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login";
    }

    @GetMapping("/admin/posts-contents")
    public String adminPostsContents() { return "admin/posts-contents"; }
}
