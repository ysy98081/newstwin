package com.est.newstwin.service;

import com.est.newstwin.domain.*;
import com.est.newstwin.dto.member.MemberRequestDto;
import com.est.newstwin.dto.member.MemberResponseDto;
import com.est.newstwin.exception.CustomException;
import com.est.newstwin.exception.ErrorCode;
import com.est.newstwin.repository.*;
import java.util.List;
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
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    /**
     * 회원가입
     * - 이메일 인증 추가 (isActive=false, 메일 전송)
     */
    public MemberResponseDto signup(MemberRequestDto requestDto) {
        if (memberRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        Member member = new Member(
                requestDto.getMemberName(),
                encodedPassword,
                requestDto.getEmail(),
                Member.Role.ROLE_USER,
                false  // 인증 전 비활성화 상태
        );

        Member savedMember = memberRepository.save(member);

        // 이메일 인증 토큰 생성 및 발송
        EmailVerificationToken token = EmailVerificationToken.create(savedMember);
        tokenRepository.save(token);
        emailService.sendVerificationEmail(savedMember.getEmail(), token.getToken());

        return MemberResponseDto.fromEntity(savedMember);
    }

    /**
     * 이메일 인증 처리
     */
    public String verifyEmail(String tokenValue) {
        EmailVerificationToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));

        if (token.isExpired()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Member member = token.getMember();
        member.setIsActive(true);
        memberRepository.save(member);

        tokenRepository.delete(token);

        return "이메일 인증이 완료되었습니다. 로그인해주세요.";
    }

    /**
     * 인증 메일 재발송
     */
    public void resendVerificationEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getIsActive()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE); // 이미 인증됨
        }

        // 기존 토큰이 있다면 삭제
        tokenRepository.findAll().stream()
                .filter(t -> t.getMember().getId().equals(member.getId()))
                .forEach(tokenRepository::delete);

        // 새 토큰 생성 후 발송
        EmailVerificationToken newToken = EmailVerificationToken.create(member);
        tokenRepository.save(newToken);
        emailService.sendVerificationEmail(member.getEmail(), newToken.getToken());
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

    public MemberResponseDto toggleSubscriptionStatus(Long memberId, List<Long> categoryIds) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        List<Category> categories = categoryRepository.findAllById(categoryIds);

        for (Category category : categories) {
            UserSubscription subscription = userSubscriptionRepository.findByMemberAndCategory(member, category)
                    .orElseGet(() -> userSubscriptionRepository.save(
                            UserSubscription.create(member, category, false)
                    ));
            subscription.setIsActive(!subscription.getIsActive());
            userSubscriptionRepository.save(subscription);
        }

        List<Category> activeCategories = userSubscriptionRepository.findAllByMember(member).stream()
                .filter(UserSubscription::getIsActive)
                .map(UserSubscription::getCategory)
                .toList();

        return MemberResponseDto.fromEntityWithCategories(member, activeCategories);
    }
}
