package com.est.newstwin.service;

import com.est.newstwin.domain.Category;
import com.est.newstwin.domain.Member;
import com.est.newstwin.domain.Post;
import com.est.newstwin.dto.post.PostDetailDto;
import com.est.newstwin.dto.post.PostSummaryDto;
import com.est.newstwin.dto.api.PostRequestDto;
import com.est.newstwin.dto.api.PostResponseDto;
import com.est.newstwin.repository.BookmarkRepository;
import com.est.newstwin.repository.CommentRepository;
import com.est.newstwin.repository.LikeRepository;
import com.est.newstwin.repository.MailLogRepository;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
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
  private final TermAnnotater termAnnotator;
  private final MemberRepository memberRepository;

  public Page<PostSummaryDto> getPosts(String category, String search, Pageable pageable) {
    final String TYPE = "news";
    final boolean ACTIVE = true;
    Page<Post> posts;

    boolean noSearch = (search == null || search.isBlank());

    if ("all".equalsIgnoreCase(category)) {
      if (noSearch) {
        posts = postRepository.findByTypeAndIsActive(TYPE, ACTIVE, pageable);
      } else {
        posts = postRepository.searchAll(TYPE, search, pageable);
      }
    } else {
      if (noSearch) {
        posts = postRepository.findByTypeAndIsActiveAndCategory_CategoryName(TYPE, ACTIVE, category, pageable);
      } else {
        posts = postRepository.searchByCategory(TYPE, category, search, pageable);
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


  @Transactional
  public Page<PostResponseDto> getBoardPosts(String type, String search, Pageable pageable) {
    if (search == null || search.trim().isEmpty()) {
      search = ""; // searchAll() 안의 LIKE 조건에 대응
    }

    Pageable sortedPageable = PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        Sort.by(Sort.Direction.DESC, "createdAt")
    );

    return postRepository.searchAll(type, search, sortedPageable)
        .map(PostResponseDto::forBoard);
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
    String annotatedContent = termAnnotator.annotate(safe(post.getContent()), dict);


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

  @Transactional
  public Long createPost(String type, PostRequestDto dto, UserDetails userDetails) {
    String email = userDetails.getUsername();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다: " + email));

    Post post = Post.builder()
        .title(dto.getTitle())
        .content(dto.getContent())
        .member(member)
        .category(null)   // 뉴스아님
        .type(type)       // 게시판
        .count(0)
        .isActive(true)
        .build();
    return postRepository.save(post).getId();
  }

  public List<PostResponseDto> getAllPost() {
    List<Post> posts = postRepository.findAll();
    return posts.stream()
        .map(post -> new PostResponseDto(post, List.of(post.getCategory())))
        .collect(Collectors.toList());
  }

  public PostResponseDto getAllPostDetail(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));
    post.increaseCount();
    List<Category> categories =
        post.getCategory() != null ? List.of(post.getCategory()) : List.of();
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


  @Transactional(readOnly = true)
  public List<PostSummaryDto> getTopPostsByTypeAndDate(String type, LocalDateTime start, LocalDateTime end, Pageable pageable) {
    List<Post> posts = postRepository.findTopByTypeAndCreatedAtBetween(type, start, end, pageable);
    return posts.stream()
            .map(post -> new PostSummaryDto(
                    post.getId(),
                    post.getTitle(),
                    post.getThumbnailUrl(),
                    post.getCreatedAt(),
                    post.getCount(),
                    abbreviate(post.getContent(), 120)
            ))
            .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<PostSummaryDto> getTopPostsByType(String type, Pageable pageable) {
    List<Post> posts = postRepository.findTopByType(type, pageable);
    return posts.stream()
            .map(post -> new PostSummaryDto(
                    post.getId(),
                    post.getTitle(),
                    post.getThumbnailUrl(),
                    post.getCreatedAt(),
                    post.getCount(),
                    abbreviate(post.getContent(), 120)
            ))
            .collect(Collectors.toList());
  }
}