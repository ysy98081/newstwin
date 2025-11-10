package com.est.newstwin.service;

import com.est.newstwin.domain.Comment;
import com.est.newstwin.domain.Member;
import com.est.newstwin.domain.Post;
import com.est.newstwin.dto.post.CommentPageResponse;
import com.est.newstwin.dto.post.NestedCommentResponseDto;
import com.est.newstwin.repository.CommentRepository;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.repository.PostRepository;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

  private final CommentRepository commentRepo;
  private final PostRepository postRepo;
  private final MemberRepository memberRepo;

  // 댓글 생성 (루트 또는 대댓글(1 depth))
  public Comment add(Long postId, Long memberId, String content, Long parentId) {

    // 입력 검증: 공백/길이
    String trimmed = content == null ? "" : content.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("댓글 내용이 없습니다.");
    }
    if (trimmed.length() > 200) {
      throw new IllegalArgumentException("댓글은 200자 이내로 입력해주세요.");
    }

    Post post = postRepo.findById(postId).orElseThrow();
    Member member = memberRepo.findById(memberId).orElseThrow();

    Comment parent = null;
    if (parentId != null) {
      parent = commentRepo.findById(parentId).orElseThrow();
      // depth 제한: parent가 루트인지 확인
      if (parent.getParent() != null) {
        throw new IllegalArgumentException("대댓글의 대댓글은 허용되지 않습니다 (depth=1 제한).");
      }
    }

    Comment comment = Comment.builder()
        .post(post)
        .member(member)
        .parent(parent)
        .content(content)
        .build();

    return commentRepo.save(comment);
  }
  /**
   * 댓글 목록: 루트만 페이징(오래된순 ASC), 각 루트의 자식은 모두 전개 (depth=1)
   * currentMemberId: 현재 로그인 사용자(없으면 null) → mine 계산용
   */
  @Transactional(readOnly = true)
  public CommentPageResponse listPaged(Long postId, int page, int size, Long currentMemberId) {
    Page<Comment> roots = commentRepo.findByPostIdAndParentIsNullOrderByCreatedAtAsc(
        postId, PageRequest.of(page, size));

    if (roots.isEmpty()) {
      return new CommentPageResponse(List.of(), false, page , 0L);
    }

    List<Long> rootIds = roots.getContent().stream().map(Comment::getId).toList();

    Map<Long, List<Comment>> childrenMap;

    // rootIds 존재할 때만 자식 조회
    if (!rootIds.isEmpty()) {
      List<Comment> children = commentRepo.findByParentIdInOrderByCreatedAtAsc(rootIds);
      childrenMap = children.stream()
          .collect(Collectors.groupingBy(c -> c.getParent().getId()));
    } else {
      childrenMap = Collections.emptyMap();
    }

    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    List<NestedCommentResponseDto> items = roots.getContent().stream().map(root -> {
      boolean mineRoot = currentMemberId != null
          && Objects.equals(root.getMember().getId(), currentMemberId);

      List<NestedCommentResponseDto> childDtos = childrenMap
          .getOrDefault(root.getId(), List.of())
          .stream()
          .map(ch -> {
            boolean mineChild = currentMemberId != null
                && Objects.equals(ch.getMember().getId(), currentMemberId);

            return new NestedCommentResponseDto(
                ch.getId(),
                root.getId(), // parentId
                ch.getMember().getMemberName(),
                ch.getContent(),
                ch.getCreatedAt().format(fmt),
                ch.isDeleted(),
                mineChild,
                ch.getMember().getProfileImage(),
                List.of()
            );
          })
          .toList();

      return new NestedCommentResponseDto(
          root.getId(),
          null,
          root.getMember().getMemberName(),
          root.getContent(),
          root.getCreatedAt().format(fmt),
          root.isDeleted(),   // 삭제되면 엔티티에서 content와 deleted 세팅됨(아래 softDelete 참고)
          mineRoot,
          root.getMember().getProfileImage(),
          childDtos
      );
    }).toList();

    boolean hasNext = roots.hasNext();
    long totalCount = commentRepo.countByPostId(postId);

    return new CommentPageResponse(items, hasNext, hasNext ? page + 1 : page, totalCount);
  }

  // 소프트 삭제
  public void softDelete(Long commentId, Long memberId, boolean isAdmin) {
    Comment comment = commentRepo.findById(commentId).orElseThrow();

    if (!isAdmin && !Objects.equals(comment.getMember().getId(), memberId)) {
      throw new AccessDeniedException("삭제 권한이 없습니다.");
    }
    // 소프트 삭제
    comment.softDelete();
  }

  //관리자 댓글 관리
  public Page<Comment> getAllComments(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    return commentRepo.findAll(pageable);
  }

  public void deleteComment(Long id) {
    Comment comment = commentRepo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    comment.setDeleted(true);
  }
}