package com.est.newstwin.service;

import com.est.newstwin.repository.MailLogRepository;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.repository.PostRepository;
import com.est.newstwin.repository.UserSubscriptionRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final MemberRepository memberRepository;
  private final PostRepository postRepository;
  private final UserSubscriptionRepository subscriptionRepository;
  private final MailLogRepository mailRepository;

  public long getUserCount() { return memberRepository.count(); }

  public long getPostCount() {
    return postRepository.count();
  }

  public long getSubscriptionCount() {
    return subscriptionRepository.count();
  }

  public long getMailCount() {
    return mailRepository.count();
  }

  private List<Long> getMonthlyCounts(int year, BiFunction<LocalDateTime, LocalDateTime, Long> countFunction) {
    List<Long> counts = new ArrayList<>();
    for (int month = 1; month <= 12; month++) {
      LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
      LocalDateTime end = start.withDayOfMonth(start.toLocalDate().lengthOfMonth())
          .withHour(23).withMinute(59).withSecond(59);
      counts.add(countFunction.apply(start, end));
    }
    return counts;
  }

  public List<Long> getMonthlyCountsForMembers(int year) {
    return getMonthlyCounts(year, memberRepository::countByCreatedAtBetween);
  }

  public List<Long> getMonthlyCountsForPosts(int year) {
    return getMonthlyCounts(year, postRepository::countByCreatedAtBetween);
  }

  public List<Long> getMonthlyCountsForSubscribers(int year) {
    return getMonthlyCounts(year, subscriptionRepository::countByCreatedAtBetween);
  }

  public List<Long> getMonthlyCountsForMails(int year) {
    return getMonthlyCounts(year, mailRepository::countByCreatedAtBetween);
  }
}