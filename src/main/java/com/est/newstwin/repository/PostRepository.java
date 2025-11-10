package com.est.newstwin.repository;


import com.est.newstwin.domain.Post;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
  //검색 없이 전체
  Page<Post> findByTypeAndIsActive(String type, boolean isActive, Pageable pageable);
  //검색 없이 카테고리
  Page<Post> findByTypeAndIsActiveAndCategory_CategoryName(String type, boolean isActive, String category, Pageable pageable);
  // 검색 + 전체
  @Query("""
    SELECT p FROM Post p
    WHERE p.type = :type
      AND p.isActive = true
      AND (
          LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
    """)
  Page<Post> searchAll(
      @Param("type") String type,
      @Param("keyword") String keyword,
      Pageable pageable);
  // 검색 + 카테고리
  @Query("""
    SELECT p FROM Post p
    WHERE p.type = :type
      AND p.isActive = true
      AND p.category.categoryName = :category
      AND (
          LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
    """)
  Page<Post> searchByCategory(
      @Param("type") String type,
      @Param("category") String category,
      @Param("keyword") String keyword,
      Pageable pageable);

  List<Post> findByType(String type);

  long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

  Page<Post> findByTypeIn(List<String> types, Pageable pageable);
}
