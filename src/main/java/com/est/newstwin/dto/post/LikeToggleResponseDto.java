package com.est.newstwin.dto.post;

public record LikeToggleResponseDto(
    boolean liked,
    long likeCount
) {}