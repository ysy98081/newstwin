package com.est.newstwin.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    // 배포/로컬 베이스 URL (미설정 시 로컬 기본값)
    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    /**
     * 이메일 인증 메일 발송 (페이지 엔드포인트로 이동)
     */
    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "NewsTwin 이메일 인증 안내";

        String verificationUrl = appBaseUrl + "/verify?token=" + token;

        String content = """
            <div style='font-family: Pretendard, sans-serif;'>
                <h2>NewsTwin 회원가입 인증</h2>
                <p>안녕하세요! 아래 버튼을 클릭해 이메일 인증을 완료해주세요.</p>
                <a href='%s'
                   style='display:inline-block;padding:12px 24px;margin-top:16px;
                          background-color:#222;color:white;text-decoration:none;
                          border-radius:6px;'>이메일 인증하기</a>
                <p style='margin-top:20px;font-size:12px;color:#888;'>이 링크는 24시간 동안만 유효합니다.</p>
            </div>
        """.formatted(verificationUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setFrom(senderEmail, "NewsTwin");
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("이메일 전송 실패", e);
        }
    }
}
