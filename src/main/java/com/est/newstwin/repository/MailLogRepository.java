package com.est.newstwin.repository;

import com.est.newstwin.domain.MailLog;
import com.est.newstwin.domain.Member;
import com.est.newstwin.domain.Post;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MailLogRepository extends JpaRepository<MailLog, Long> {
  List<MailLog> findAllByMember(Member member);
  List<MailLog> findAllByPost(Post post);
  List<MailLog> findAllByStatus(String status);

  void deleteAllByPostId(Long postId);

  List<MailLog> findAllByPost_TitleAndPost_Type(String title, String type);

  long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}