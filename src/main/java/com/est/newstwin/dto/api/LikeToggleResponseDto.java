package com.est.newstwin.dto.api;

public record LikeToggleResponseDto(
    boolean liked,
    long likeCount
) {}