package com.est.newstwin.controller.page;

import com.est.newstwin.dto.post.PostDetailDto;
import com.est.newstwin.dto.post.PostSummaryDto;
import com.est.newstwin.service.PostService;
import com.est.newstwin.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
      // 최신순 + 조회수 tie-break
      sortObj = Sort.by(
          sort.contains("desc") ? Sort.Order.desc("createdAt") : Sort.Order.asc("createdAt"),
          Sort.Order.desc("count")   // tie breaker
      );
    } else if (sort.startsWith("count")) {
      // 인기순 + 날짜 tie-break
      sortObj = Sort.by(
          sort.contains("desc") ? Sort.Order.desc("count") : Sort.Order.asc("count"),
          Sort.Order.desc("createdAt") // tie breaker
      );
    } else {
      // 기본 fallback
      sortObj = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("count"));
    }

    PageRequest pageable = PageRequest.of(Math.max(page, 0), 10, sortObj);

    Page<PostSummaryDto> posts = postService.getPosts(category, search, pageable);

    model.addAttribute("posts", posts.getContent());
    model.addAttribute("page", posts);
    model.addAttribute("categoryName", category);
    model.addAttribute("sort", sort);
    model.addAttribute("search", search);

    String email = (userDetails != null ? userDetails.getUsername() : null);
    model.addAttribute("categories", subscriptionService.getCategorySidebar(email));
    model.addAttribute("isLoggedIn", userDetails != null);

    return "news/list";
  }

  @GetMapping("/post/{id}")
  public String getPostDetail(@PathVariable Long id, Model model) {
    PostDetailDto post = postService.getPostDetail(id);
    model.addAttribute("post", post);
    return "news/detail";
  }
}