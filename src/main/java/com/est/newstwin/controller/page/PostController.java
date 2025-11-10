package com.est.newstwin.controller.page;

import com.est.newstwin.domain.Member;
import com.est.newstwin.dto.post.PostDetailDto;
import com.est.newstwin.dto.post.PostSummaryDto;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.service.PostService;
import com.est.newstwin.service.LikeService;
import com.est.newstwin.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;
  private final SubscriptionService subscriptionService;
  private final LikeService likeService;
  private final MemberRepository memberRepository;

  @GetMapping("/news")
  public String getFeed(
      @RequestParam(required = false) String category,
      @RequestParam(defaultValue = "createdAt,desc") String sort,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(required = false) String search,
      @AuthenticationPrincipal UserDetails userDetails,
      Model model
  ) {
    if (category == null || category.isBlank()) {
      category = "all";
    }

    Sort sortObj;
    if (sort.startsWith("createdAt")) {
      // 최신순 + 조회수
      sortObj = Sort.by(
          sort.contains("desc") ? Sort.Order.desc("createdAt") : Sort.Order.asc("createdAt"),
          Sort.Order.desc("count")   // tie breaker
      );
    } else if (sort.startsWith("count")) {
      // 인기순 + 날짜
      sortObj = Sort.by(
          sort.contains("desc") ? Sort.Order.desc("count") : Sort.Order.asc("count"),
          Sort.Order.desc("createdAt") // tie breaker
      );
    } else {
      // 기본
      sortObj = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("count"));
    }

    PageRequest pageable = PageRequest.of(Math.max(page, 0), 10, sortObj);

    Page<PostSummaryDto> posts = postService.getPosts(category, search, pageable);

    boolean memberReceiveEmail = false;
    String email = null;

    if (userDetails != null) {
      email = userDetails.getUsername();
      Member member = memberRepository.findByEmail(email).get();
      memberReceiveEmail = Boolean.TRUE.equals(member.getReceiveEmail());
    }

    model.addAttribute("posts", posts.getContent());
    model.addAttribute("page", posts);
    model.addAttribute("categoryName", category);
    model.addAttribute("sort", sort);
    model.addAttribute("search", search);
    model.addAttribute("categories", subscriptionService.getCategorySidebar(email));
    //로그인 여부
    model.addAttribute("isLoggedIn", userDetails != null);
    //구독여부
    model.addAttribute("memberReceiveEmail", memberReceiveEmail);

    return "news/list";
  }

  @GetMapping("/post/{id}")
  public String getPostDetail(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
    PostDetailDto post = postService.getPostDetail(id);
    long likeCount = likeService.count(post.getId());

    Long memberId = null;
    String email = null;
    boolean liked = false;
    boolean memberReceiveEmail = false;

    if (userDetails != null) {
      email = userDetails.getUsername();
      Member member = memberRepository.findByEmail(email).get();
      memberId = member.getId();
      liked = likeService.isLiked(post.getId(), memberId);
      memberReceiveEmail = Boolean.TRUE.equals(member.getReceiveEmail());
    }

    //기본
    model.addAttribute("post", post);
    model.addAttribute("likeCount", likeCount);
    model.addAttribute("liked", liked);
    //사이드바 카테고리용
    model.addAttribute("categories", subscriptionService.getCategorySidebar(email));
    //로그인여부
    model.addAttribute("isLoggedIn", userDetails != null);
    //구독여부
    model.addAttribute("memberReceiveEmail", memberReceiveEmail);
    return "news/detail";
  }
}