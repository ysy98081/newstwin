package com.est.newstwin.service;

import com.est.newstwin.domain.MailLog;
import com.est.newstwin.domain.Member;
import com.est.newstwin.domain.Post;
import com.est.newstwin.repository.MailLogRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailLogService {

  private final JavaMailSender mailSender;
  private final MailLogRepository mailLogRepository;
  private final ChatGPTService chatGPTService;

  public List<MailLog> getAllMailLogs() {
    return mailLogRepository.findAll();
  }

  public void updateMailStatus(Long mailId, String newStatus) {
    MailLog log = mailLogRepository.findById(mailId)
        .orElseThrow(() -> new IllegalArgumentException("Mail log not found"));
    log.setStatus(newStatus);
    log.setLastAttemptAt(LocalDateTime.now());
    mailLogRepository.save(log);
  }

  @Async
  @Transactional
  public void sendNewsletterAsync(Member member, String summary, Post mailPost, List<Post> newsPosts)
  {
    try {
      String html = buildHtmlNewsletter(member, summary, newsPosts);
      sendEmail(member.getEmail(), "[NewsTwin] 오늘의 맞춤 뉴스레터", html);

      mailLogRepository.save(MailLog.builder()
          .member(member)
          .post(mailPost)
          .status("SUCCESS")
          .createdAt(LocalDateTime.now())
          .build());

      log.info("✅ [{}] 통합 뉴스레터 발송 완료", member.getEmail());

    } catch (Exception e) {
      mailLogRepository.save(MailLog.builder()
          .member(member)
          .post(mailPost)
          .status("FAIL")
          .errorMessage(e.getMessage())
          .createdAt(LocalDateTime.now())
          .build());
      log.error("❌ [{}] 뉴스레터 발송 실패: {}", member.getEmail(), e.getMessage());
    }
  }

  private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(htmlContent, true);
    mailSender.send(message);
  }

  public String buildHtmlNewsletter(Member member, String summary, List<Post> newsPosts) {
    String unsubscribeLink = "http://localhost:8080/mypage/subscription";

    List<Post> distinctPosts = newsPosts.stream()
        .collect(Collectors.toMap(
            p -> (p.getTitle() + "_" + p.getCategory().getId()),
            p -> p,
            (a, b) -> a
        ))
        .values()
        .stream()
        .toList();

    StringBuilder newsLinks = new StringBuilder();
    for (Post p : distinctPosts) {
      newsLinks.append("<li>")
          .append("<a href='http://localhost:8080/post/")
          .append(p.getId())
          .append("' style='color:#007bff;text-decoration:none;'>")
          .append(p.getTitle())
          .append("</a>")
          .append("</li>");
    }

    return """
        <div style='font-family:Arial,sans-serif;padding:20px;'>
          <h2>📬 NewsTwin 통합 뉴스레터</h2>
          <p>안녕하세요, %s님 👋</p>
          <p>오늘의 AI 뉴스 요약:</p>
          <blockquote style='background:#f5f5f5;padding:10px;border-radius:8px;'>%s</blockquote>
          <p><strong>📎 참고한 뉴스 목록</strong></p>
          <ul>%s</ul>
          <hr>
          <p style='font-size:12px;color:#888;'>
            뉴스레터 수신 거부는 <a href='%s' style='color:#888;'>여기서 해제</a> 가능합니다.
          </p>
        </div>
        """.formatted(member.getMemberName(), summary, newsLinks, unsubscribeLink);
  }

  @Transactional
  public void resendMail(Long mailId) {
    MailLog log = mailLogRepository.findById(mailId)
        .orElseThrow(() -> new IllegalArgumentException("메일 로그를 찾을 수 없습니다."));

    Member member = log.getMember();
    Post mailPost = log.getPost();

    if (mailPost == null) {
      throw new IllegalStateException("이 메일 로그에 연결된 메일 Post가 없습니다.");
    }

    try {
      String subject = "[NewsTwin 재전송] " + mailPost.getTitle();
      String htmlContent = mailPost.getContent();

      sendEmail(member.getEmail(), subject, htmlContent);

      log.setStatus("SUCCESS");
      log.setRetryCount(log.getRetryCount() + 1);
      log.setLastAttemptAt(LocalDateTime.now());
      mailLogRepository.save(log);
    } catch (Exception e) {
      log.setStatus("FAIL");
      log.setRetryCount(log.getRetryCount() + 1);
      log.setErrorMessage(e.getMessage());
      log.setLastAttemptAt(LocalDateTime.now());
      mailLogRepository.save(log);
      throw new RuntimeException("메일 재전송 실패", e);
    }
  }
}
