package com.est.newstwin.repository;

import com.est.newstwin.domain.Member;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByEmail(String email);

  boolean existsByEmail(String email);

  long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

  long countByReceiveEmailTrue();

  long countByReceiveEmailTrueAndUpdatedAtBetween(LocalDateTime start, LocalDateTime end);

  @Query("""
    SELECT DISTINCT m
    FROM Member m
    LEFT JOIN FETCH m.subscriptions s
    LEFT JOIN FETCH s.category
    WHERE m.receiveEmail = true 
      AND m.isActive = true
      AND m.isVerified = true
""")
  List<Member> findAllActiveSubscribers();
}
