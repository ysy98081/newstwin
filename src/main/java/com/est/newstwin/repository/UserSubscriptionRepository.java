package com.est.newstwin.repository;

import com.est.newstwin.domain.Category;
import com.est.newstwin.domain.Member;
import com.est.newstwin.domain.UserSubscription;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
  List<UserSubscription> findAllByMember(Member member); // 단방향 조회용
  List<UserSubscription> findAllByCategory(Category category);
  Optional<UserSubscription> findByMemberAndCategory(Member member, Category category);


  Optional<UserSubscription> findByMemberAndCategoryId(Member member, Long categoryId);

  @Modifying
  @Query("UPDATE UserSubscription us SET us.isActive = false WHERE us.member = :member")
  void deactivateAllByMember(@Param("member") Member member);
  
  // subscribeAll 최적화용 (회원 + 여러 카테고리 한번에 조회)
  @Query("select us from UserSubscription us where us.member = :member and us.category.id in :categoryIds")
  List<UserSubscription> findAllByMemberAndCategoryIdIn(@Param("member") Member member,
      @Param("categoryIds") List<Long> categoryIds);
}