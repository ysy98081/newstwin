package com.est.newstwin.controller.page;

import com.est.newstwin.domain.Post;
import com.est.newstwin.dto.api.PostDetailDto;
import com.est.newstwin.dto.api.PostSummaryDto;
import com.est.newstwin.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

  @GetMapping("/feed")
  public String getFeed(
      @RequestParam(defaultValue = "all") String category,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
      Model model
  ) {
    Page<PostSummaryDto> posts = postService.getPosts(category, pageable);

    model.addAttribute("posts", posts.getContent());
    model.addAttribute("page", posts);
    model.addAttribute("categoryName", category);
    model.addAttribute("sort", pageable.getSort().toString());

    return "news/list";
  }


  @GetMapping("/post/{id}")
  public String getPostDetail(@PathVariable Long id, Model model) {
    PostDetailDto post = postService.getPostDetail(id);
    model.addAttribute("post", post);
    return "news/detail";
  }
}