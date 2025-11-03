package com.est.newstwin.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String memberName;  // 닉네임
  private String password;    // 비밀번호
  private String email;       // 이메일(로그인 ID)

  @Enumerated(EnumType.STRING)
  private Role role;          // 역할

  private Boolean receiveEmail = true;  // 전체 뉴스레터 수신 동의
  private String profileImage;          // 프로필 사진

  private LocalDateTime createdAt;  // 생성일자
  private LocalDateTime updatedAt;  // 수정일자

  public Member(String memberName, String password, String email, Role role) {
    this.memberName = memberName;
    this.password = password;
    this.email = email;
    this.role = role;
    this.receiveEmail = true;
    this.profileImage = "/images/basic-profile.png";
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public void updateInfo(String newName, String newPassword, Boolean newReceiveEmail, String newProfileImage) {
    if (newName != null && !newName.isBlank()) {
      this.memberName = newName;
    }
    if (newPassword != null && !newPassword.isBlank()) {
      this.password = newPassword;
    }
    if (newReceiveEmail != null) {
      this.receiveEmail = newReceiveEmail;
    }
    if (newProfileImage != null && !newProfileImage.isBlank()) {
      this.profileImage = newProfileImage;
    }
    this.updatedAt = LocalDateTime.now();
  }


  public enum Role {
    ROLE_USER,
    ROLE_ADMIN
  }
}
