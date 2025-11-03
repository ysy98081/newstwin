package com.est.newstwin.dto.post;

import org.antlr.v4.runtime.misc.NotNull;

public record BookmarkToggleRequestDto (
    @NotNull Long bookmarkId
){}