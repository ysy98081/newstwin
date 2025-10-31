package com.est.newstwin.dto.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostSummaryDto {
  private Long id;
  private String title;
  private String thumbnailUrl;
  private String content; // 요약용
}