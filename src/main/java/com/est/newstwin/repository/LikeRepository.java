package com.est.newstwin.repository;

import com.est.newstwin.domain.Like;
import com.est.newstwin.domain.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
  boolean existsByPostIdAndMemberId(Long postId, Long memberId);
  long countByPostId(Long postId);
  void deleteByPostIdAndMemberId(Long postId, Long memberId);

  @Modifying
  @Query("DELETE FROM Like l WHERE l.post.id = :postId")
  void deleteAllByPostId(@Param("postId") Long postId);
}