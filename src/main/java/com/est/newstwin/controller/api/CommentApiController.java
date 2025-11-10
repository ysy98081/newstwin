package com.est.newstwin.controller.api;

import com.est.newstwin.domain.Comment;
import com.est.newstwin.domain.Member;
import com.est.newstwin.dto.post.CommentCreateRequest;
import com.est.newstwin.dto.post.CommentPageResponse;
import com.est.newstwin.dto.post.NestedCommentResponseDto;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.service.CommentService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class CommentApiController {

  private final CommentService commentService;
  private final MemberRepository memberRepository;

  // 목록 (공개) /api/posts/{postId}/comments?page=0&size=20
  @GetMapping("/{postId}/comments")
  public ResponseEntity<CommentPageResponse> list(
      @PathVariable Long postId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    Long memberId = null;
    if(userDetails != null){
      memberId = memberRepository.findByEmail(userDetails.getUsername())
          .map(Member::getId)
          .orElse(null);
    }
    if (page < 0) page = 0;
    if (size < 1 || size > 100) size = 20; // 20 default
    return ResponseEntity.ok(commentService.listPaged(postId, page, size, memberId));
  }

  // 작성 (로그인 필요)
  @PostMapping("/{postId}/comments")
  public ResponseEntity<NestedCommentResponseDto> create(
      @PathVariable Long postId,
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestBody CommentCreateRequest req
  ) {
    if (userDetails == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    Member me = memberRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

    Comment saved = commentService.add(postId, me.getId(), req.getContent(), req.getParentId());

    NestedCommentResponseDto dto = new NestedCommentResponseDto(
        saved.getId(),
        saved.getParent() == null ? null : saved.getParent().getId(),
        me.getMemberName(),
        saved.getContent(),
        saved.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
        saved.isDeleted(),
        true,
        me.getProfileImage(),
        List.of()
    );
    return ResponseEntity.ok(dto);
  }

  // 삭제 (작성자 또는 ADMIN) - 소프트 삭제
  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<Void> delete(
      @PathVariable Long commentId,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    if (userDetails == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    Member me = memberRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

    boolean isAdmin = me.getRole() != null && me.getRole().name().contains("ADMIN");
    commentService.softDelete(commentId, me.getId(), isAdmin);
    return ResponseEntity.noContent().build();
  }
}