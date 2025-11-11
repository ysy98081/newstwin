package com.est.newstwin.repository;

import com.est.newstwin.domain.Member;
import com.est.newstwin.domain.Post;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findByEmail(String email);

  long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

  @Query("SELECT m FROM Member m WHERE m.receiveEmail = true AND m.isActive = true")
  List<Member> findAllActiveSubscribers();
}
