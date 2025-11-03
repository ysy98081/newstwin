package com.est.newstwin.dto.post;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostDetailDto {
  private Long id;
  private String title;
  private String content;
  private String thumbnailUrl;
  private String categoryName;
  private LocalDateTime createdAt;
  private int count;
}
