package com.est.newstwin.service;

import com.est.newstwin.domain.Post;
import com.est.newstwin.dto.post.PostDetailDto;
import com.est.newstwin.dto.post.PostSummaryDto;
import com.est.newstwin.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;

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
    return posts.map(post ->
        new PostSummaryDto(
            post.getId(),
            post.getTitle(),
            post.getThumbnailUrl(),
            post.getCreatedAt(),
            post.getCount(),
            abbreviate(post.getContent(), 120) // 요약 생성
        )
    );
  }
  // 문자열 요약 유틸
  private String abbreviate(String text, int length) {
    if (text == null) return "";
    return text.length() > length ? text.substring(0, length) + "..." : text;
  }

  @Transactional
  public PostDetailDto getPostDetail(Long id) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Post not found: " + id));

    post.increaseCount();

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