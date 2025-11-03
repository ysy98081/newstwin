package com.est.newstwin.service;

import com.est.newstwin.domain.Category;
import com.est.newstwin.domain.Member;
import com.est.newstwin.domain.UserSubscription;
import com.est.newstwin.dto.member.MemberRequestDto;
import com.est.newstwin.dto.member.MemberResponseDto;
import com.est.newstwin.exception.CustomException;
import com.est.newstwin.exception.ErrorCode;
import com.est.newstwin.repository.CategoryRepository;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.repository.UserSubscriptionRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;

    /**
     * 회원가입
     * - 이메일 중복 검사 및 비밀번호 암호화 후 저장
     */
    public MemberResponseDto signup(MemberRequestDto requestDto) {
        // 이메일 중복 검사
        if (memberRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // Member 엔티티 생성
        Member member = new Member(
                requestDto.getMemberName(),
                encodedPassword,
                requestDto.getEmail(),
                Member.Role.ROLE_USER,
                true
        );

        // 저장 및 응답 DTO 변환
        Member savedMember = memberRepository.save(member);

        return MemberResponseDto.fromEntity(savedMember);
    }

  @Transactional(readOnly = true)
  public List<MemberResponseDto> getAllMembers() {
    return memberRepository.findAll().stream()
        .map(member -> {
          List<UserSubscription> subscriptions = userSubscriptionRepository.findAllByMember(member);

          List<String> categoryNames = subscriptions.stream()
              .map(sub -> sub.getCategory().getCategoryName())
              .toList();

          boolean hasActive = subscriptions.stream().anyMatch(UserSubscription::getIsActive);
          String subscriptionStatus = hasActive ? "구독중" : "구독 없음";

          return MemberResponseDto.builder()
              .id(member.getId())
              .memberName(member.getMemberName())
              .email(member.getEmail())
              .role(member.getRole().name())
              .isActive(member.getIsActive())
              .categories(categoryNames)
              .categoryIds(subscriptions.stream()
                  .map(s -> s.getCategory().getId())
                  .toList())
              .subscriptionStatus(subscriptionStatus)
              .build();
        })
        .toList();
  }

  public MemberResponseDto toggleMemberStatus(Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new RuntimeException("회원 없음"));

    member.setIsActive(!member.getIsActive());
    memberRepository.save(member);

    List<Category> activeCategories = userSubscriptionRepository.findAllByMember(member).stream()
        .filter(UserSubscription::getIsActive)
        .map(UserSubscription::getCategory)
        .toList();

    return MemberResponseDto.fromEntityWithCategories(member, activeCategories);
  }

  public MemberResponseDto toggleSubscriptionStatus(Long memberId, Long categoryId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new RuntimeException("회원 없음"));

    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new RuntimeException("카테고리 없음"));

    UserSubscription subscription = userSubscriptionRepository.findByMemberAndCategory(member, category)
        .orElseGet(() -> userSubscriptionRepository.save(
            UserSubscription.create(member, category, false)
        ));

    subscription.setIsActive(!subscription.getIsActive());
    userSubscriptionRepository.save(subscription);

    List<Category> activeCategories = userSubscriptionRepository.findAllByMember(member).stream()
        .filter(UserSubscription::getIsActive)
        .map(UserSubscription::getCategory)
        .toList();

    return MemberResponseDto.fromEntityWithCategories(member, activeCategories);
  }
}
