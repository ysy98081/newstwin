package com.est.newstwin.dto.post;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostSummaryDto {
  private Long id;
  private String title;
  private String thumbnailUrl;
  LocalDateTime createdAt;
  int count;
  private String content; // 요약용
}