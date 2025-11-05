package com.est.newstwin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(indexes = {
    @Index(name = "unique_terms", columnList = "term", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Term {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "term_id")
  private Long id;

  @Column(nullable = false, length = 50, unique = true)
  private String term;

  @Column(nullable = false, length = 100)
  private String definition;

  @Column(name = "created_at", columnDefinition = "TIMESTAMP(0)")
  private LocalDateTime createdAt;

  @Column(name = "updated_at", columnDefinition = "TIMESTAMP(0)")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now().withNano(0);
    this.updatedAt = LocalDateTime.now().withNano(0);
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now().withNano(0);
  }
}
