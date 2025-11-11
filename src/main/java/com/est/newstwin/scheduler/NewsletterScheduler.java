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
  private final ChatGPTService chatGPTService;
  private final MailLogService mailService;

  /** ✅ 수동 실행용 (GET /newsletter) */
  @Transactional
  @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
  public void sendNewsletters() {
    log.info("📧 [NewsletterScheduler] 구독자별 통합 뉴스 생성 및 발송 시작");

    List<Member> subscribers = memberRepository.findAllActiveSubscribers();
    if (subscribers.isEmpty()) {
      log.warn("🚫 구독자 없음");
      return;
    }

    LocalDateTime since = LocalDateTime.now().minusDays(1);

    for (Member member : subscribers) {
      // ✅ 1️⃣ 구독 카테고리 확인
      List<Category> subscribedCategories = member.getSubscriptions().stream()
          .filter(UserSubscription::getIsActive)
          .map(UserSubscription::getCategory)
          .collect(Collectors.toList());

      if (subscribedCategories.isEmpty()) {
        log.info("⚠️ {}님은 활성화된 구독 카테고리가 없습니다.", member.getEmail());
        continue;
      }

      // ✅ 2️⃣ 카테고리별 최신 뉴스 수집
      List<Post> allRecentNews = new ArrayList<>();
      for (Category category : subscribedCategories) {
        List<Post> recentNews = postRepository.findRecentNewsByCategory(category.getId(), since);
        if (!recentNews.isEmpty()) {
          allRecentNews.addAll(recentNews);
        }
      }

      if (allRecentNews.isEmpty()) {
        log.info("🚫 {}님에게 보낼 뉴스 없음", member.getEmail());
        continue;
      }

      try {
        // ✅ 3️⃣ GPT 요약 생성
        String sourceText = buildSummaryText(allRecentNews);
        String markdown = chatGPTService.analyzeMarkdown(sourceText);
        String json = chatGPTService.analyzeJson(markdown);
        String title = chatGPTService.generateTitle(markdown);

        // ✅ 4️⃣ 메일용 HTML 생성
        String htmlContent = mailService.buildHtmlNewsletter(member, markdown, allRecentNews);

        // ✅ 5️⃣ 메일 post 저장
        Post mailPost = Post.builder()
            .member(member) // 수신자 기준 저장
            .category(subscribedCategories.get(0))
            .type("mail")
            .title("[NewsTwin] " + title)
            .content(htmlContent)
            .analysisJson(json)
            .isActive(true)
            .count(0)
            .build();
        postRepository.save(mailPost);

        // ✅ 6️⃣ 메일 발송 + MailLog 기록
        mailService.sendNewsletterAsync(member, markdown, mailPost, allRecentNews);
        log.info("📨 {}님에게 통합 뉴스레터 발송 완료", member.getEmail());

      } catch (Exception e) {
        log.error("❌ {}님 뉴스 생성/발송 중 오류: {}", member.getEmail(), e.getMessage());
      }
    }

    log.info("✅ [NewsletterScheduler] 전체 구독자 뉴스 발송 완료");
  }

  /** ✅ GPT 입력용 뉴스 텍스트 생성 */
  private String buildSummaryText(List<Post> posts) {
    StringBuilder sb = new StringBuilder();
    for (Post post : posts) {
      sb.append("제목: ").append(post.getTitle()).append("\n")
          .append("내용: ")
          .append(post.getContent(), 0, Math.min(300, post.getContent().length()))
          .append("...\n\n");
    }
    return sb.toString();
  }
}
