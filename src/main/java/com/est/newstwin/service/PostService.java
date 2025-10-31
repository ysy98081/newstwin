package com.est.newstwin.service;

import com.est.newstwin.domain.Post;
import com.est.newstwin.dto.api.PostDetailDto;
import com.est.newstwin.dto.api.PostSummaryDto;
import com.est.newstwin.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;

  public Post getPostById(Long id) {
    return postRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
  }

  public Page<PostSummaryDto> getPosts(String category, Pageable pageable) {
    Page<Post> posts;

    if ("all".equalsIgnoreCase(category)) {
      posts = postRepository.findAll(pageable);
    } else {
      posts = postRepository.findByCategoryCategoryName(category, pageable);
    }

    // null 방어 — 빈 페이지 반환
    if (posts == null) {
      return Page.empty(pageable);
    }

    // Entity → DTO 변환
    return posts.map(p ->
        new PostSummaryDto(
            p.getId(),
            p.getTitle(),
            p.getThumbnailUrl(),
            abbreviate(p.getContent(), 120) // 요약 생성
        )
    );
  }
  // 문자열 요약 유틸
  private String abbreviate(String text, int length) {
    if (text == null) return "";
    return text.length() > length ? text.substring(0, length) + "..." : text;
  }

  public PostDetailDto getPostDetail(Long id) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Post not found: " + id));

    return new PostDetailDto(
        post.getId(),
        post.getTitle(),
        post.getContent(),
        post.getThumbnailUrl(),
        post.getCategory().getCategoryName(),
        post.getCreatedAt(),
        post.getCount()
    );
  }
}