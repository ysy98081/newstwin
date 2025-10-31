package com.est.newstwin.dto.api;

public record LikeStateResponseDto(
    boolean liked,
    long likeCount
) {}