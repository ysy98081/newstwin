package com.est.newstwin.service;

import com.est.newstwin.domain.Category;
import com.est.newstwin.domain.Member;
import com.est.newstwin.domain.UserSubscription;
import com.est.newstwin.dto.api.CategoryViewDto;
import com.est.newstwin.repository.CategoryRepository;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.repository.UserSubscriptionRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

  private final MemberRepository memberRepository;
  private final CategoryRepository categoryRepository;
  private final UserSubscriptionRepository userSubscriptionRepository;

  @Transactional
  public boolean toggleCategory(Long categoryId, String userEmail) {
    Member member = memberRepository.findByEmail(userEmail)
        .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new IllegalArgumentException("카테고리 없음"));

    UserSubscription us = userSubscriptionRepository.findByMemberAndCategory(member, category)
        .orElse(null);

    if (us == null) {
      userSubscriptionRepository.save(UserSubscription.createTrue(member, category));
      return true;
    } else {
      boolean newActive = !Boolean.TRUE.equals(us.getIsActive());
      us.setIsActive(newActive);
      return newActive;
    }
  }

  @Transactional
  public int subscribeAll(String userEmail) {
    Member member = memberRepository.findByEmail(userEmail)
        .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

    List<Category> all = categoryRepository.findAll();
    List<Long> ids = all.stream().map(Category::getId).toList();

    List<UserSubscription> mine =
        userSubscriptionRepository.findAllByMemberAndCategoryIdIn(member, ids);

    Map<Long, UserSubscription> map = mine.stream()
        .collect(Collectors.toMap(us -> us.getCategory().getId(), Function.identity()));

    int activated = 0;

    for (Category c : all) {
      UserSubscription us = map.get(c.getId());
      if (us == null) {
        userSubscriptionRepository.save(UserSubscription.createTrue(member, c));
        activated++;
      } else if (!Boolean.TRUE.equals(us.getIsActive())) {
        us.setIsActive(true);
        activated++;
      }
    }
    return activated;
  }

  @Transactional(readOnly = true)
  public List<CategoryViewDto> getCategorySidebar(String email) {

    List<Category> all = categoryRepository.findAll();

    if (email == null || email.isBlank()) {
      return all.stream().map(c -> new CategoryViewDto(c.getId(), c.getCategoryName(), false)).toList();
    }

    Member member = memberRepository.findByEmail(email).orElse(null);
    if (member == null) {
      return all.stream().map(c -> new CategoryViewDto(c.getId(), c.getCategoryName(), false)).toList();
    }

    List<UserSubscription> mine = userSubscriptionRepository.findAllByMember(member);
    Set<Long> active = mine.stream()
        .filter(us -> Boolean.TRUE.equals(us.getIsActive()))
        .map(us -> us.getCategory().getId())
        .collect(Collectors.toSet());

    return all.stream()
        .map(c -> new CategoryViewDto(c.getId(), c.getCategoryName(), active.contains(c.getId())))
        .toList();
  }
}