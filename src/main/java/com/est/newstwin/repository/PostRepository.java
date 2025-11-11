package com.est.newstwin.repository;

import com.est.newstwin.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 전체
    Page<Post> findByTypeAndIsActive(String type, boolean isActive, Pageable pageable);

    // 카테고리별 (isActive=true 필터 포함)
    Page<Post> findByTypeAndIsActiveAndCategory_CategoryName(
            String type,
            boolean isActive,
            String category,
            Pageable pageable
    );

    Page<Post> findByCategoryCategoryName(String categoryName, Pageable pageable);

    List<Post> findByType(String type);

    Page<Post> findByTypeIn(List<String> types, Pageable pageable);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);


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
            Pageable pageable
    );

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
            Pageable pageable
    );

    // 기간별 인기글 (좋아요 기준)
    @Query("""
        SELECT p FROM Post p
        LEFT JOIN p.likes l
        WHERE p.type = :type
          AND p.createdAt BETWEEN :start AND :end
        GROUP BY p.id, p.title, p.thumbnailUrl, p.createdAt, p.count, p.content, p.category
        ORDER BY COUNT(l) DESC, p.createdAt DESC
        """)
    List<Post> findTopByTypeAndCreatedAtBetween(
            @Param("type") String type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    // 전체 인기글 (좋아요 순)
    @Query("""
        SELECT p FROM Post p
        LEFT JOIN p.likes l
        WHERE p.type = :type
        GROUP BY p.id, p.title, p.thumbnailUrl, p.createdAt, p.count, p.content, p.category
        ORDER BY COUNT(l) DESC, p.createdAt DESC
        """)
    List<Post> findTopByType(
            @Param("type") String type,
            Pageable pageable
    );

  @Query("SELECT p FROM Post p WHERE p.category.id = :categoryId AND p.type = 'news' AND p.createdAt >= :since")
  List<Post> findRecentNewsByCategory(@Param("categoryId") Long categoryId, @Param("since") LocalDateTime since);

}
