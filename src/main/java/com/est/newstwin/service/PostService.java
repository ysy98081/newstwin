package com.est.newstwin.service;

import com.est.newstwin.domain.Category;
import com.est.newstwin.domain.Post;
import com.est.newstwin.dto.post.PostDetailDto;
import com.est.newstwin.dto.post.PostSummaryDto;
import com.est.newstwin.dto.api.PostRequestDto;
import com.est.newstwin.dto.api.PostResponseDto;
import com.est.newstwin.repository.BookmarkRepository;
import com.est.newstwin.repository.CommentRepository;
import com.est.newstwin.repository.LikeRepository;
import com.est.newstwin.repository.MailLogRepository;
import com.est.newstwin.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final BookmarkRepository bookmarkRepository;
  private final CommentRepository commentRepository;
  private final MailLogRepository mailLogRepository;
  private final LikeRepository likeRepository;
  private final TermCacheService termCacheService;
  private final TermAnnotater TermAnnotator;

  public Page<PostSummaryDto> getPosts(String category, String search, Pageable pageable) {
    Page<Post> posts;

    boolean noSearch = (search == null || search.isBlank());

    if ("all".equalsIgnoreCase(category)) {
      if (noSearch) {
        posts = postRepository.findAll(pageable);
      } else {
        posts = postRepository.searchAll(search, pageable);
      }
    } else {
      if (noSearch) {
        posts = postRepository.findByCategoryCategoryName(category, pageable);
      } else {
        posts = postRepository.searchByCategory(category, search, pageable);
      }
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

  private String safe(String s) {
    return s == null ? "" : s;
  }

  @Transactional
  public PostDetailDto getPostDetail(Long id) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Post not found: " + id));

    post.increaseCount();

    Map<String,String> dict = termCacheService.dict();
    String annotatedContent = TermAnnotator.annotate(safe(post.getContent()), dict);


    return new PostDetailDto(
        post.getId(),
        post.getTitle(),
        annotatedContent,
        post.getThumbnailUrl(),
        post.getCategory().getCategoryName(),
        post.getCreatedAt(),
        post.getCount()
    );
  }

  public List<PostResponseDto> getAllPost() {
    List<Post> posts = postRepository.findAll();
    return posts.stream()
        .map(post -> new PostResponseDto(post, List.of(post.getCategory()))) // Post가 Category 하나만 가질 경우
        .collect(Collectors.toList());
  }

  public PostResponseDto getAllPostDetail(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));

    List<Category> categories = List.of(post.getCategory());
    return new PostResponseDto(post, categories);
  }

  @Transactional
  public PostResponseDto togglePostStatus(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));

    post.setIsActive(!post.getIsActive());
    postRepository.save(post);
    List<Category> categories = List.of(post.getCategory());
    return new PostResponseDto(post, categories);
  }

  public List<PostResponseDto> getPostsByType(String type) {
    List<Post> posts;

    if (type == null || type.isEmpty()) {
      posts = postRepository.findAll();
    } else {
      posts = postRepository.findByType(type);
    }

    return posts.stream()
        .map(post -> new PostResponseDto(post, List.of(post.getCategory())))
        .collect(Collectors.toList());
  }

  @Transactional
  public void updatePost(Long postId, PostRequestDto dto) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));

    post.setTitle(dto.getTitle());
    post.setContent(dto.getContent());
    post.setUpdatedAt(LocalDateTime.now());
  }

  @Transactional
  public void deletePost(Long postId) {
    commentRepository.deleteAllByPostId(postId);
    bookmarkRepository.deleteAllByPostId(postId);
    mailLogRepository.deleteAllByPostId(postId);
    likeRepository.deleteAllByPostId(postId);
    postRepository.deleteById(postId);
  }
}