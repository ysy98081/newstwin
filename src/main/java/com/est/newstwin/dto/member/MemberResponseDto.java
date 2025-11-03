package com.est.newstwin.dto.member;

import com.est.newstwin.domain.Category;
import com.est.newstwin.domain.Member;
import java.util.List;
import java.util.stream.Collectors;
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
    private Boolean isActive;
    private List<String> categories;
    private List<Long> categoryIds;
    private String subscriptionStatus;

    public static MemberResponseDto fromEntity(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .memberName(member.getMemberName())
                .email(member.getEmail())
                .role(member.getRole().name())
                .build();
    }

  public static MemberResponseDto fromEntityWithCategories(Member member, List<Category> categories) {
    List<String> categoryNames = categories.stream()
        .map(Category::getCategoryName)
        .toList();

    List<Long> categoryIds = categories.stream()
        .map(Category::getId)
        .toList();

    return MemberResponseDto.builder()
        .id(member.getId())
        .memberName(member.getMemberName())
        .email(member.getEmail())
        .role(member.getRole().name())
        .isActive(member.getIsActive())
        .categories(categoryNames)
        .categoryIds(categoryIds)
        .subscriptionStatus(categoryNames.isEmpty() ? "구독 없음" : "구독중")
        .build();
  }
}
