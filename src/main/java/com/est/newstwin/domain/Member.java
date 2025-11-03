package com.est.newstwin.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "member")
@Getter
@Setter
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
  
  @Column(name = "status", nullable = false)
  private Boolean isActive = true;

  private LocalDateTime createdAt;  // 생성일자
  private LocalDateTime updatedAt;  // 수정일자

  // 연관관계 매핑 추가
  @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
  private List<UserSubscription> subscriptions;

  // 구독 카테고리명 리스트 반환 (HTML에서 사용 가능)
  public List<String> getCategories() {
    if (subscriptions == null) return List.of();
    return subscriptions.stream()
        .filter(UserSubscription::getIsActive)
        .map(s -> s.getCategory().getCategoryName())
        .collect(Collectors.toList());
  }

  public Member(String memberName, String password, String email, Role role, Boolean status) {
    this.memberName = memberName;
    this.password = password;
    this.email = email;
    this.role = role;
    this.receiveEmail = true;
    this.profileImage = "/images/basic-profile.png";
    this.isActive = status;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public void update(String memberName, String password, Boolean status) {
    this.memberName = memberName;
    this.password = password;
    this.isActive = status;
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
