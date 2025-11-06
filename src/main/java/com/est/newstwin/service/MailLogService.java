package com.est.newstwin.service;

import com.est.newstwin.domain.MailLog;
import com.est.newstwin.repository.MailLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailLogService {

  private final MailLogRepository mailLogRepository;

  public List<MailLog> getAllMailLogs() {
    List<MailLog> result = mailLogRepository.findAllByPost_Type("mail");
    System.out.println("📨 Mail logs count = " + result.size());
    return result;
  }

  // 상태 업데이트
  public void updateMailStatus(Long mailId, String newStatus) {
    MailLog log = mailLogRepository.findById(mailId)
        .orElseThrow(() -> new IllegalArgumentException("Mail log not found"));
    log.setStatus(newStatus);
    log.setLastAttemptAt(LocalDateTime.now());
    mailLogRepository.save(log);
  }
}

