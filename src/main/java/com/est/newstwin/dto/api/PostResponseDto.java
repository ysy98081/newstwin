package com.est.newstwin.dto.api;

import com.est.newstwin.domain.Category;
import com.est.newstwin.domain.Post;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDto {
  private Long id;
  private Long memberId;
  private String memberName;
  private String title;
  private String content;
  private String thumbnailUrl;
  private List<String> categories;
  private int count;
  private Boolean isActive;
  private String type;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public PostResponseDto(Post post, List<Category> categories) {
    this.id = post.getId();
    this.memberId = post.getMember().getId();
    this.memberName = post.getMember().getMemberName();
    this.title = post.getTitle();
    this.content = post.getContent();
    this.thumbnailUrl = post.getThumbnailUrl();
    this.isActive = post.getIsActive();
    this.count = post.getCount();
    this.type = post.getType();
    this.createdAt = post.getCreatedAt();
    this.updatedAt = post.getUpdatedAt();

    if (categories != null && !categories.isEmpty()) {
      this.categories = categories.stream()
          .map(Category::getCategoryName)
          .collect(Collectors.toList());
    } else {
      this.categories = List.of();
    }
  }

  // 게시판 전용 (정적 팩토리)
  public static PostResponseDto forBoard(Post post) {
    return new PostResponseDto(post, List.of());
  }
}
