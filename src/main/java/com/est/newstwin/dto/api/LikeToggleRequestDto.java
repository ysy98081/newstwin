package com.est.newstwin.dto.api;

import org.antlr.v4.runtime.misc.NotNull;

public record LikeToggleRequestDto(
    @NotNull Long postId
) {}