package com.est.newstwin.repository;

import com.est.newstwin.domain.Comment;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

  // 루트 댓글(부모 없음) 페이징
  @EntityGraph(attributePaths = {"member"}) // 작성자 N+1 방지
  Page<Comment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId, Pageable pageable);

  // 특정 루트들의 자식들 한 번에 조회 (depth=1)
  @EntityGraph(attributePaths = {"member", "parent"})
  List<Comment> findByParentIdInOrderByCreatedAtAsc(List<Long> parentIds);

  @Modifying
  @Query("DELETE FROM Comment c WHERE c.post.id = :postId")
  void deleteAllByPostId(@Param("postId") Long postId);

  long countByPostId(Long postId);
}
