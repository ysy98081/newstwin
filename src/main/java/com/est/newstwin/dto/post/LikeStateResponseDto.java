package com.est.newstwin.dto.post;

public record LikeStateResponseDto(
    boolean liked,
    long likeCount
) {}