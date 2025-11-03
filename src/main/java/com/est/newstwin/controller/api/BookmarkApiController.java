package com.est.newstwin.controller.api;

import com.est.newstwin.domain.Member;
import com.est.newstwin.dto.post.BookmarkStateResponseDto;
import com.est.newstwin.dto.post.BookmarkToggleResponseDto;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.service.BookmarkService;
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
public class BookmarkApiController {
  private final BookmarkService bookmarkService;
  private final MemberRepository memberRepository;

  @PostMapping("/{postId}/bookmark")
  public ResponseEntity<BookmarkToggleResponseDto> toggleBookmark(@PathVariable Long postId,
      @AuthenticationPrincipal UserDetails userDetails) {
    Member member = memberRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
    boolean bookmarked = bookmarkService.toggle(postId, member.getId());
    return ResponseEntity.ok(new BookmarkToggleResponseDto(bookmarked));
  }

  @GetMapping("/{postId}/bookmark")
  public ResponseEntity<BookmarkStateResponseDto> getBookmarkState(@PathVariable Long postId,
      @AuthenticationPrincipal UserDetails userDetails) {
    Member member = memberRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
    boolean bookmarked = bookmarkService.isBookmarked(postId, member.getId());
    return ResponseEntity.ok(new BookmarkStateResponseDto(bookmarked));
  }
}
