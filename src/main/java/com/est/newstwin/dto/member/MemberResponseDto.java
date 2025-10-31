package com.est.newstwin.dto.member;

import com.est.newstwin.domain.Member;
import lombok.Getter;
import lombok.Builder;

/**
 * 회원 응답 DTO
 * - 회원 정보(Entity → DTO 변환)를 외부로 반환
 */

@Getter
@Builder
public class MemberResponseDto {
    private Long id;
    private String memberName;
    private String email;
    private String role;

    public static MemberResponseDto fromEntity(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .memberName(member.getMemberName())
                .email(member.getEmail())
                .role(member.getRole().name())
                .build();
    }
}
