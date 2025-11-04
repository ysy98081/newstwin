package com.est.newstwin.dto.mypage;

import com.est.newstwin.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkPostResponseDto {
    private Long postId;
    private String title;
    private String categoryName;
    private String thumbnailUrl;
    private String createdAt;

    public static BookmarkPostResponseDto from(Post post) {
        return BookmarkPostResponseDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .categoryName(post.getCategory().getCategoryName())
                .thumbnailUrl(post.getThumbnailUrl())
                .createdAt(
                        post.getCreatedAt() != null
                                ? post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
                                : null
                )
                .build();
    }
}
