package com.est.newstwin.dto.post;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NestedCommentResponseDto {
  private Long id;
  private Long parentId;      // 루트면 null
  private String authorName;
  private String content;
  private String createdAt;   // yyyy-MM-dd HH:mm
  private boolean deleted;
  private boolean mine;
  private String profileImage;
  private List<NestedCommentResponseDto> children; // depth=1까지만 채움
}