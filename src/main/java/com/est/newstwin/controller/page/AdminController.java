package com.est.newstwin.controller.page;

import com.est.newstwin.domain.MailLog;
import com.est.newstwin.dto.api.PostRequestDto;
import com.est.newstwin.dto.api.PostResponseDto;
import com.est.newstwin.dto.member.MemberResponseDto;
import com.est.newstwin.repository.MailLogRepository;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.repository.PostRepository;
import com.est.newstwin.repository.UserSubscriptionRepository;
import com.est.newstwin.service.AdminService;
import com.est.newstwin.service.MailLogService;
import com.est.newstwin.service.MemberService;
import com.est.newstwin.service.PostService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class AdminController {
  private final MemberService memberService;
  private final PostService postService;
  private final AdminService dashboardService;
  private final MailLogService mailLogService;
  private final MailLogRepository mailLogRepository;

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

  //게시판 전체 조회
  @GetMapping("admin/posts")
  public String getPostList(Model model) {
    List<PostResponseDto> posts = postService.getAllPost()
        .stream()
        .filter(post -> "news".equals(post.getType()) || "community".equals(post.getType()))
        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
        .toList();

    model.addAttribute("posts", posts);
    return "admin/posts";
  }

  // 게시판 회원 상태 토글
  @PatchMapping("admin/posts/{postId}/status")
  @ResponseBody
  public PostResponseDto togglePostStatus(@PathVariable Long postId) {
    return postService.togglePostStatus(postId);
  }

  @GetMapping("/admin/posts/filter")
  public String filterPostList(@RequestParam(required = false) String type, Model model) {
    List<PostResponseDto> posts = postService.getPostsByType(type);
    model.addAttribute("posts", posts);
    return "admin/posts :: postTableBody";
  }

  // 게시글 상세 보기
  @GetMapping("admin/posts-contents/{postId}")
  public String getAllPostDetail(@PathVariable Long postId, Model model) {
    PostResponseDto post = postService.getAllPostDetail(postId);
    model.addAttribute("post", post);
    return "admin/posts-contents";
  }

  @PostMapping("/admin/posts-contents/{id}/action")
  public String handlePostAction(
      @PathVariable Long id,
      @ModelAttribute PostRequestDto dto,
      @RequestParam String action) {

    if ("edit".equals(action)) {
      postService.updatePost(id, dto);
      return "redirect:/admin/posts-contents/" + id;
    } else if ("delete".equals(action)) {
      postService.deletePost(id);
      return "redirect:/admin/posts";
    }
    return "redirect:/admin/posts";
  }

  @GetMapping("/admin")
  public String dashboard(Model model) {
    // 서비스에서 데이터 받아오기
    long userCount = dashboardService.getUserCount();
    long postCount = dashboardService.getPostCount();
    long subscriptionCount = dashboardService.getSubscriptionCount();
    long mailCount = dashboardService.getMailCount();

    // Thymeleaf에 전달
    model.addAttribute("userCount", userCount);
    model.addAttribute("postCount", postCount);
    model.addAttribute("subscriptionCount", subscriptionCount);
    model.addAttribute("mailCount", mailCount);

    return "admin/dashboard";
  }

  // 메일 로그 목록 페이지
  @GetMapping("/admin/mails")
  public String getMailLogs(Model model) {
    List<MailLog> logs = mailLogService.getAllMailLogs()
        .stream()
        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
        .toList();

    model.addAttribute("mailLogs", logs);
    return "admin/mails";
  }


  // 메일 상태 업데이트
  @PostMapping("/admin/mails/update-status")
  @ResponseBody
  public ResponseEntity<Void> updateMailStatus(@RequestParam Long mailId, @RequestParam String status) {
    mailLogService.updateMailStatus(mailId, status);
    return ResponseEntity.ok().build();
  }

  // 메일 상세 페이지 (메일 타입만)
  @GetMapping("/admin/mails-contents")
  public String viewMailContents(@RequestParam String title, Model model) {
    List<MailLog> newsLogs = mailLogRepository.findAllByPost_TitleAndPost_Type(title, "mail");
    if (newsLogs.isEmpty()) {
      model.addAttribute("newsTitle", "메일 로그가 없습니다.");
    } else {
      model.addAttribute("newsTitle", newsLogs.get(0).getPost().getTitle());
    }
    model.addAttribute("newsLogs", newsLogs);
    return "admin/mails-contents";
  }
}
