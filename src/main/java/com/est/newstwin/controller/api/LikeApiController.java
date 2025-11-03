package com.est.newstwin.controller.api;

import com.est.newstwin.domain.Member;
import com.est.newstwin.dto.post.LikeStateResponseDto;
import com.est.newstwin.dto.post.LikeToggleResponseDto;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class LikeApiController {

  private final LikeService likeService;
  private final MemberRepository memberRepository;

  @PostMapping("/{postId}/like")
  public ResponseEntity<LikeToggleResponseDto> toggleLike(@PathVariable Long postId,
      @AuthenticationPrincipal UserDetails userDetails) {
    Member member = memberRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
    var result = likeService.toggle(postId, member.getId());
    return ResponseEntity.ok(new LikeToggleResponseDto(result.isLiked(), result.getLikeCount()));
  }

  // 좋아요 상태 조회
  @GetMapping("/{postId}/like")
  public ResponseEntity<LikeStateResponseDto> getLikeState(@PathVariable Long postId,
      @AuthenticationPrincipal UserDetails userDetails) {
    Member member = memberRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
    boolean liked = likeService.isLiked(postId, member.getId());
    long likeCount = likeService.count(postId);
    return ResponseEntity.ok(new LikeStateResponseDto(liked, likeCount));
  }


/* 시큐리티 완성 이후 적용
  // 좋아요 토글
  @PostMapping("/{postId}/like")
  public ResponseEntity<LikeToggleResponseDto> toggleLike(@PathVariable Long postId,
      @AuthenticationPrincipal Member member) {
    var r = likeService.toggle(postId, member.getId());
    return ResponseEntity.ok(new LikeToggleResponseDto(r.isLiked(), r.getLikeCount()));
  }

  // 좋아요 상태 조회
  @GetMapping("/{postId}/like")
  public ResponseEntity<LikeStateResponseDto> getLikeState(@PathVariable Long postId,
      @AuthenticationPrincipal Member member) {
    boolean liked = likeService.isLiked(postId, member.getId());
    long likeCount = likeService.count(postId);
    return ResponseEntity.ok(new LikeStateResponseDto(liked, likeCount));
  }*/
}