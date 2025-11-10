package com.est.newstwin.controller.page;

import com.est.newstwin.domain.Comment;
import com.est.newstwin.domain.MailLog;
import com.est.newstwin.domain.Member;
import com.est.newstwin.dto.api.PostRequestDto;
import com.est.newstwin.dto.api.PostResponseDto;
import com.est.newstwin.dto.auth.LoginRequestDto;
import com.est.newstwin.dto.auth.LoginResponseDto;
import com.est.newstwin.dto.member.MemberResponseDto;
import com.est.newstwin.exception.CustomException;
import com.est.newstwin.exception.ErrorCode;
import com.est.newstwin.repository.MailLogRepository;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.service.AdminService;
import com.est.newstwin.service.AuthService;
import com.est.newstwin.service.CommentService;
import com.est.newstwin.service.MailLogService;
import com.est.newstwin.service.MemberService;
import com.est.newstwin.service.PostService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class AdminController {
  private final MemberService memberService;
  private final PostService postService;
  private final AdminService adminService;
  private final MailLogService mailLogService;
  private final MailLogRepository mailLogRepository;
  private final CommentService commentService;
  private final AuthService authService;
  private final MemberRepository memberRepository;

  @GetMapping("admin/users")
  public String getMemberList(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              Model model) {
    List<MemberResponseDto> members = memberService.getAllMembers();

    int start = page * size;
    int end = Math.min(start + size, members.size());
    List<MemberResponseDto> paged = members.subList(start, end);
    Page<MemberResponseDto> membersPage = new PageImpl<>(paged, PageRequest.of(page, size), members.size());
    model.addAttribute("members", membersPage.getContent());
    model.addAttribute("page", membersPage);
    return "admin/users";
  }

  // 회원 상태 토글
  @PatchMapping("/admin/users/{memberId}/status")
  @ResponseBody
  public MemberResponseDto toggleMemberStatus(@PathVariable Long memberId) {
    return memberService.toggleMemberStatus(memberId);
  }

  // 구독 상태 토글
  @PatchMapping("/admin/users/{memberId}/subscriptions")
  @ResponseBody
  public ResponseEntity<MemberResponseDto> toggleSubscriptions(
      @PathVariable Long memberId,
      @RequestBody List<Long> categoryIds) {

    MemberResponseDto dto = memberService.toggleSubscriptionStatus(memberId, categoryIds);
    return ResponseEntity.ok(dto);
  }

  //게시판 전체 조회
  @GetMapping("/admin/posts")
  public String getPostList(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) String type,
                            Model model) {
    List<PostResponseDto> allPosts = postService.getAllPost()
        .stream()
        .filter(post -> "news".equals(post.getType()) || "community".equals(post.getType()))
        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
        .toList();

    if (type != null && !type.isEmpty()) {
      allPosts = allPosts.stream()
          .filter(post -> type.equals(post.getType()))
          .toList();
    }

    int start = page * size;
    int end = Math.min(start + size, allPosts.size());
    List<PostResponseDto> pagedPosts = allPosts.subList(start, end);
    Page<PostResponseDto> postPage = new PageImpl<>(pagedPosts, PageRequest.of(page, size), allPosts.size());
    model.addAttribute("posts", postPage.getContent());
    model.addAttribute("page", postPage);
    model.addAttribute("selectedType", type);
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
    long userCount = adminService.getUserCount();
    long postCount = adminService.getPostCount();
    long subscriptionCount = adminService.getSubscriptionCount();
    long mailCount = adminService.getMailCount();

    model.addAttribute("userCount", userCount);
    model.addAttribute("postCount", postCount);
    model.addAttribute("subscriptionCount", subscriptionCount);
    model.addAttribute("mailCount", mailCount);

    return "admin/dashboard";
  }

  @GetMapping("admin/dashboard/monthly-counts")
  @ResponseBody
  public Map<String, Object> getMonthlyCounts(@RequestParam int year) {
    Map<String, Object> result = new HashMap<>();
    result.put("months", List.of("1월","2월","3월","4월","5월","6월","7월","8월","9월","10월","11월","12월"));
    result.put("userCounts", adminService.getMonthlyCountsForMembers(year));
    result.put("postCounts", adminService.getMonthlyCountsForPosts(year));
    result.put("subscriberCounts", adminService.getMonthlyCountsForSubscribers(year));
    result.put("mailCounts", adminService.getMonthlyCountsForMails(year));
    return result;
  }

  // 메일 로그 목록 페이지
  @GetMapping("/admin/mails")
  public String getMailLogs(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {
    List<MailLog> logs = mailLogService.getAllMailLogs()
        .stream()
        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
        .toList();

    int start = page * size;
    int end = Math.min(start + size, logs.size());
    List<MailLog> pagedLogs = logs.subList(start, end);
    Page<MailLog> mailPage = new PageImpl<>(pagedLogs, PageRequest.of(page, size), logs.size());
    model.addAttribute("mailLogs", pagedLogs);
    model.addAttribute("page", mailPage);
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

  //댓글 페이지
  @GetMapping("admin/comments")
  public String getCommentsList(@RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      Model model) {
    Page<Comment> commentsPage = commentService.getAllComments(page, size);
    model.addAttribute("comments", commentsPage.getContent());
    model.addAttribute("page", commentsPage);
    return "admin/comments";
  }

  @PostMapping("/admin/comments/{id}/delete")
  @ResponseBody
  public ResponseEntity<?> deleteComment(@PathVariable Long id) {
    commentService.deleteComment(id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/admin/login")
  public ResponseEntity<?> adminLogin( @Valid @RequestBody LoginRequestDto requestDto,
      HttpServletResponse response) {

    LoginResponseDto loginResponse = authService.login(requestDto);
    Member member = memberRepository.findByEmail(requestDto.getEmail())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    if (!"ROLE_ADMIN".equals(member.getRole().name())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of(
              "success", false,
              "message", "관리자 권한이 없습니다."
          ));
    }

    ResponseCookie cookie = ResponseCookie.from("accessToken", loginResponse.getAccessToken())
        .httpOnly(true)
        .secure(false)
        .path("/")
        .maxAge(Duration.ofHours(1))
        .sameSite("Lax")
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    return ResponseEntity.ok(Map.of(
        "success", true,
        "message", "관리자 로그인 성공",
        "memberName", loginResponse.getMemberName()
    ));
  }

  @PostMapping("/admin/logout")
  @ResponseBody
  public ResponseEntity<?> logout(HttpServletResponse response) {
    ResponseCookie expiredCookie = ResponseCookie.from("accessToken", "")
        .path("/")
        .maxAge(0)
        .httpOnly(true)
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
    return ResponseEntity.ok(Map.of("success", true, "message", "로그아웃 완료"));
  }

  @ModelAttribute("isAdminLoggedIn")
  public boolean isAdminLoggedIn() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth != null && auth.isAuthenticated() &&
        !"anonymousUser".equals(auth.getPrincipal());
  }

}
