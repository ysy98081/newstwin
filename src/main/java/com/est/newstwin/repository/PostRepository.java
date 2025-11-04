package com.est.newstwin.repository;


import com.est.newstwin.domain.Post;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
  Page<Post> findByCategoryCategoryName(String categoryName, Pageable pageable);
  // 전체 검색 (category = all) 제목기반으로 요구서에 적혀있는데 내용도 검색하게 일단 구현
  @Query("""
      SELECT p FROM Post p
      WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
         OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
      """)
  Page<Post> searchAll(@Param("keyword") String keyword, Pageable pageable);
  // 특정 category + 검색
  @Query("""
      SELECT p FROM Post p
      WHERE p.category.categoryName = :category
        AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
      """)
  Page<Post> searchByCategory(@Param("category") String category,
      @Param("keyword") String keyword,
      Pageable pageable);

  List<Post> findByType(String type);
}
