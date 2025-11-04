package com.est.newstwin.repository;

import com.est.newstwin.domain.Bookmark;
import com.est.newstwin.domain.Member;
import com.est.newstwin.domain.Post;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

  // 특정 회원이 단일 게시글을 북마크했는지 확인
  Optional<Bookmark> findByMemberAndPost(Member member, Post post);

  // 특정 회원이 북마크한 모든 게시글 조회
  List<Bookmark> findAllByMember(Member member);
  //게시물에 해당하는 북마크 확인
  boolean existsByPostIdAndMemberId(Long postId, Long memberId);
  //게시물에 해당하는 북마크 삭제
  void deleteByPostIdAndMemberId(Long postId, Long memberId);
  //게시물에 해당하는 전체 북마크 삭제
  @Modifying
  @Query("DELETE FROM Bookmark b WHERE b.post.id = :postId")
  void deleteAllByPostId(@Param("postId") Long postId);
}
