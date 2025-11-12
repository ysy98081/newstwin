package com.est.newstwin.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailVerificationResponseDto {
    private boolean success;
    private String message;
}
