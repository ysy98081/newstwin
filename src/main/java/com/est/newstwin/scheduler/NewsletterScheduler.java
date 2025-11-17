package com.est.newstwin.scheduler;

import com.est.newstwin.domain.Category;
import com.est.newstwin.domain.Member;
import com.est.newstwin.domain.Post;
import com.est.newstwin.domain.UserSubscription;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.repository.PostRepository;
import com.est.newstwin.service.ChatGPTService;
import com.est.newstwin.service.MailLogService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsletterScheduler {

  private final MemberRepository memberRepository;
  private final PostRepository postRepository;
  private final MailLogService mailService;

  @Transactional
  @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
  public void sendNewsletters() {
    log.info("📧 [NewsletterScheduler] 구독자별 뉴스 발송 시작");

    List<Member> subscribers = memberRepository.findAllActiveSubscribers();
    if (subscribers.isEmpty()) {
      log.warn("🚫 구독자 없음");
      return;
    }

    LocalDateTime since = LocalDateTime.now().minusDays(1);

    for (Member member : subscribers) {
      List<Category> subscribedCategories = member.getSubscriptions().stream()
          .filter(UserSubscription::getIsActive)
          .map(UserSubscription::getCategory)
          .collect(Collectors.toList());

      if (subscribedCategories.isEmpty()) {
        log.info("⚠️ {}님 활성 구독 카테고리 없음", member.getEmail());
        continue;
      }

      List<Post> allRecentNews = new ArrayList<>();
      for (Category category : subscribedCategories) {
        List<Post> recentNews = postRepository.findRecentNewsByCategory(category.getId(), since);
        if (!recentNews.isEmpty()) {
          allRecentNews.addAll(recentNews);
        }
      }

      if (allRecentNews.isEmpty()) {
        log.info("🚫 {}님에게 보낼 원본 뉴스 없음", member.getEmail());
        continue;
      }

      try {
        String htmlContent = mailService.buildHtmlFromOriginalPosts(member, allRecentNews);

        Post mailPost = Post.builder()
            .member(member)
            .category(subscribedCategories.get(0))
            .type("mail")
            .title("[NewsTwin] 오늘의 뉴스레터")
            .content(htmlContent)
            .analysisJson(null)
            .isActive(true)
            .count(0)
            .build();
        postRepository.save(mailPost);

        mailService.sendNewsletterAsync(member, htmlContent, mailPost, allRecentNews);
        log.info("📨 {}님에게 원본 뉴스레터 발송 완료", member.getEmail());

      } catch (Exception e) {
        log.error("❌ {}님 뉴스 발송 오류: {}", member.getEmail(), e.getMessage());
      }
    }
    log.info("✅ 전체 구독자 뉴스 발송 완료");
  }

}
