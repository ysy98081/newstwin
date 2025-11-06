package com.est.newstwin.dto.post;

import lombok.Getter;
  // 요청
  @Getter
  public class CommentCreateRequest {
    private String content;
    private Long parentId; // 루트면 null, 대댓글이면 루트 댓글의 id
}
