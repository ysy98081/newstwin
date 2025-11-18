package com.est.newstwin.service;

import com.est.newstwin.domain.MailLog;
import com.est.newstwin.domain.Member;
import com.est.newstwin.domain.Post;
import com.est.newstwin.repository.MailLogRepository;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
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
    String unsubscribeLink = String.format(
        "https://newstwin.kro.kr/api/members/unsubscribe?memberId=%d", member.getId()
    );

    for (Post p : newsPosts) {
      String localLink = "https://newstwin.kro.kr/post/" + p.getId();
      String linkHtml = String.format(
          "<a href='%s' target='_blank' style='color:#0d6efd; text-decoration:none; font-weight:500;'>%s</a>",
          localLink, p.getTitle()
      );
      summary = summary.replaceFirst("\\(뉴스 링크: URL\\)", linkHtml);
    }

    MutableDataSet options = new MutableDataSet();
    Parser parser = Parser.builder(options).build();
    HtmlRenderer renderer = HtmlRenderer.builder(options).build();

    Node document = parser.parse(summary);
    String summaryHtml = renderer.render(document);

    return """
        <div style='font-family:Arial,Helvetica,sans-serif; background-color:#f8f9fa; padding:20px;'>
          <div style='min-width:600px; margin:auto; background:#ffffff; padding:25px; border-radius:10px; box-shadow:0 2px 8px rgba(0,0,0,0.05);'>
            <h2 style='font-size:22px; color:#333333; margin-top:0;'>📬 NewsTwin 통합 뉴스레터</h2>
            <p style='font-size:15px; color:#333;'>안녕하세요, <strong>%s</strong>님 👋</p>
        
            <div style='margin-top:15px; background:#f3f6fb; border-left:4px solid #007bff; padding:15px 20px; border-radius:6px;'>
              %s
            </div>
        
            <div style='margin-top:25px; font-size:12px; color:#888888; text-align:center;'>
              <hr style='border:none; border-top:1px solid #eee; margin:15px 0;'>
              뉴스레터 수신 거부는
              <a href='%s' style='color:#d9534f; text-decoration:none;'>여기</a>를 클릭하세요.<br>
              © 2025 NewsTwin. All rights reserved.
            </div>
          </div>
        </div>
        """.formatted(member.getMemberName(), summaryHtml, unsubscribeLink);
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

  public String buildHtmlFromOriginalPosts(Member member, List<Post> posts) {

    StringBuilder sb = new StringBuilder();
    String baseUrl = "https://newstwin.kro.kr";

    sb.append("<div style='font-family:Arial, Helvetica, sans-serif; padding:20px;'>")
        .append("<h2>오늘의 뉴스레터</h2>")
        .append("<p>").append(member.getMemberName()).append(" 님, 오늘 등록된 뉴스입니다.</p>")
        .append("<hr>");

    for (Post post : posts) {

      String postUrl = baseUrl + "/post/" + post.getId();
      String content = post.getContent();

      if (!content.toLowerCase().contains("<p") && content.contains("![")) {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        content = renderer.render(parser.parse(content));
      }

      content = content.replaceAll(
          "src=[\"'](/uploads/[^\"']+)[\"']",
          "src=\"" + baseUrl + "$1\""
      );

      sb.append("<div style='margin-bottom:20px;'>")
          .append("<h3>").append(post.getTitle()).append("</h3>")
          .append("<p>")
          .append("<a href='").append(postUrl).append("' target='_blank' ")
          .append("style='color:#0d6efd; text-decoration:none; font-weight:500;'>원문 읽기</a>")
          .append("</p>")
          .append("<div style='line-height:1.6;'>")
          .append(content)
          .append("</div>")
          .append("</div>")
          .append("<hr>");
    }

    sb.append("</div>");
    return sb.toString();
  }
}
