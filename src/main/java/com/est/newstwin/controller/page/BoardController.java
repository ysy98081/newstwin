package com.est.newstwin.controller.page;

import com.est.newstwin.domain.Member;
import com.est.newstwin.dto.api.PostRequestDto;
import com.est.newstwin.dto.api.PostResponseDto;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.repository.PostRepository;
import com.est.newstwin.service.LikeService;
import com.est.newstwin.service.PostService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

  private final PostService postService;
  private final MemberRepository memberRepository;
  private final LikeService likeService;

  /** 게시글 목록 */
  @GetMapping
  public String list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(required = false) String search,
      Model model) {

    Page<PostResponseDto> posts = postService.getBoardPosts("community", search, PageRequest.of(page, 10));
    model.addAttribute("posts", posts);
    model.addAttribute("search", search);
    return "board/list";
  }

  /** 게시글 상세보기 */
  @GetMapping("/{id}")
  public String detail(
      @PathVariable Long id,
      Model model,
      @AuthenticationPrincipal UserDetails userDetails) {

    PostResponseDto post = postService.getAllPostDetail(id);
    long likeCount = likeService.count(post.getId());

    Long memberId = null;
    boolean liked = false;
    boolean isAuthor = false;

    if (userDetails != null) {
      Member member = memberRepository.findByEmail(userDetails.getUsername())
          .orElseThrow(() -> new RuntimeException("Member not found"));
      memberId = member.getId();
      liked = likeService.isLiked(post.getId(), memberId);

      if (Objects.equals(post.getMemberId(), memberId)) {
        isAuthor = true;
      }
    }
    model.addAttribute("post", post);
    model.addAttribute("likeCount", likeCount);
    model.addAttribute("liked", liked);
    model.addAttribute("isAuthor", isAuthor);
    model.addAttribute("isLoggedIn", userDetails != null);

    return "board/detail";
  }

  // 쓰기 & 수정 공용 폼
  @GetMapping({"/write", "/edit/{id}"})
  public String form(@PathVariable(required = false) Long id, Model model,
      @AuthenticationPrincipal UserDetails userDetails) {
    String username = userDetails.getUsername();
    model.addAttribute("username", username);

    if (id != null) { // 수정 폼
      PostResponseDto post = postService.getAllPostDetail(id);
      model.addAttribute("post", post);
      model.addAttribute("isEdit", true);
    } else { // 새 글 폼
      model.addAttribute("post", null);
      model.addAttribute("isEdit", false);
    }
    return "board/form";
  }

 //쓰기 & 수정 공용 처리
  @PostMapping({"/write", "/edit/{id}"})
  public String submit(@PathVariable(required = false) Long id,
                       @ModelAttribute PostRequestDto dto,
      @AuthenticationPrincipal UserDetails userDetails) {
    Long postId;
    if (id != null) { // 수정
      postService.updatePost(id, dto);
      postId = id;
    } else { // 신규 작성
      postId = postService.createPost("community", dto, userDetails);
    }
    return "redirect:/board/" + postId;
  }

  // 삭제
  @DeleteMapping("/delete/{id}")
  @ResponseBody
  public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    postService.deletePost(id);
    return ResponseEntity.ok().build();
  }
}