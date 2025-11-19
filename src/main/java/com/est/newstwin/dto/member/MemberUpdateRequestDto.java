package com.est.newstwin.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * 회원 정보 수정 요청 DTO
 * - 닉네임, 비밀번호, 이메일 수신 여부, 프로필 이미지
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateRequestDto {
    private String memberName;
    private String password;
    private Boolean receiveEmail;
    private MultipartFile profileImage;
    // TEMP에 저장된 이미지 URL
    private String tempImageUrl;
}
