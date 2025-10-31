package com.est.newstwin.service;

import com.est.newstwin.domain.Like;
import com.est.newstwin.domain.Member;
import com.est.newstwin.domain.Post;
import com.est.newstwin.repository.LikeRepository;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.repository.PostRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

  private final PostRepository postRepository;
  private final MemberRepository memberRepository;
  private final LikeRepository likeRepository;

  @Transactional
  public ToggleResult toggle(Long postId, Long memberId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

    boolean already = likeRepository.existsByPostIdAndMemberId(postId, memberId);

    if (already) {
      likeRepository.deleteByPostIdAndMemberId(postId, memberId);
    } else {
      try {
        likeRepository.save(Like.builder().post(post).member(member).build());
      } catch (DataIntegrityViolationException ignored) { /* 동시성 중복 클릭 방어 */ }
    }
    long count = likeRepository.countByPostId(postId);
    return new ToggleResult(!already, count);
  }

  @Transactional(readOnly = true)
  public boolean isLiked(Long postId, Long memberId) {
    return likeRepository.existsByPostIdAndMemberId(postId, memberId);
  }

  @Transactional(readOnly = true)
  public long count(Long postId) {
    return likeRepository.countByPostId(postId);
  }

  @Getter @AllArgsConstructor
  public static class ToggleResult {
    private final boolean liked;
    private final long likeCount;
  }
}