package com.est.newstwin.dto.post;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentPageResponse {
  private List<NestedCommentResponseDto> items;
  private boolean hasNext;
  private int nextPage;       // 다음 페이지 번호 (hasNext=false면 의미없음)
  private long totalCount;
}