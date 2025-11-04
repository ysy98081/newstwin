package com.est.newstwin.service;

import com.est.newstwin.domain.Category;
import com.est.newstwin.domain.Member;
import com.est.newstwin.domain.UserSubscription;
import com.est.newstwin.dto.member.MemberResponseDto;
import com.est.newstwin.dto.member.MemberUpdateRequestDto;
import com.est.newstwin.dto.mypage.SubscriptionRequestDto;
import com.est.newstwin.dto.mypage.SubscriptionResponseDto;
import com.est.newstwin.exception.CustomException;
import com.est.newstwin.exception.ErrorCode;
import com.est.newstwin.repository.CategoryRepository;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
/**
 * 마이페이지 관련 비즈니스 로직
 * - 회원 정보 조회 및 수정
 */
@Service
@RequiredArgsConstructor
public class MypageService {

    private final MemberRepository memberRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    /** 내 정보 조회 */
    @Transactional(readOnly = true)
    public MemberResponseDto getMyInfo(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberResponseDto.fromEntity(member);
    }

    /** 내 정보 수정 (입력된 값만 반영) */
    @Transactional
    public MemberResponseDto updateMyInfo(String email, MemberUpdateRequestDto dto) {

        System.out.println(">>> 요청 DTO = " + dto);
        System.out.println(">>> memberName = " + dto.getMemberName());
        System.out.println(">>> password = " + dto.getPassword());
        System.out.println(">>> receiveEmail = " + dto.getReceiveEmail());
        System.out.println(">>> profileImage = " + (dto.getProfileImage() != null ? dto.getProfileImage().getOriginalFilename() : null));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        boolean changed = false;

        // 1) 비밀번호 변경
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            String encoded = passwordEncoder.encode(dto.getPassword());
            member.updateInfo(null, encoded, null, null);
            changed = true;
        }

        // 2) 닉네임 변경
        if (dto.getMemberName() != null && !dto.getMemberName().isBlank()
                && !dto.getMemberName().equals(member.getMemberName())) {
            member.updateInfo(dto.getMemberName(), null, null, null);
            changed = true;
        }

        // 3) 이메일 수신 여부 변경
        if (dto.getReceiveEmail() != null && !dto.getReceiveEmail().equals(member.getReceiveEmail())) {
            member.updateInfo(null, null, dto.getReceiveEmail(), null);
            changed = true;
        }

        // 4) 프로필 이미지 변경
        if (dto.getProfileImage() != null && !dto.getProfileImage().isEmpty()) {
            try {
                String rootDir = System.getProperty("user.dir"); // 현재 프로젝트 루트 절대경로
                Path uploadDir = Paths.get(rootDir, "uploads", "profile");
                Files.createDirectories(uploadDir); // 경로 없으면 자동 생성

                String fileName = UUID.randomUUID() + "_" + dto.getProfileImage().getOriginalFilename();
                Path savePath = uploadDir.resolve(fileName);

                dto.getProfileImage().transferTo(savePath.toFile());

                member.updateInfo(null, null, null, "/uploads/profile/" + fileName);
                changed = true;
            } catch (IOException e) {
                e.printStackTrace();
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        }

        if (changed) {
            Member saved = memberRepository.save(member);
            System.out.println(">>> 변경된 회원 저장 완료: " + saved.getId());
        } else {
            System.out.println(">>> 변경 없음 - 업데이트 스킵");
        }

        return MemberResponseDto.fromEntity(member);
    }

    /** 구독 목록 조회 */
    @Transactional(readOnly = true)
    public SubscriptionResponseDto getSubscriptions(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<String> categories = member.getSubscriptions().stream()
                .filter(UserSubscription::getIsActive)
                .map(us -> us.getCategory().getCategoryName())
                .collect(Collectors.toList());

        return SubscriptionResponseDto.builder()
                .receiveEmail(member.getReceiveEmail())
                .subscribedCategories(categories)
                .build();
    }

    /** 구독 설정 저장 */
    @Transactional
    public void updateSubscriptions(String email, SubscriptionRequestDto request) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 전체 수신 여부 업데이트
        member.setReceiveEmail(request.getReceiveEmail());
        memberRepository.save(member);

        // 기존 구독 비활성화
        userSubscriptionRepository.deactivateAllByMember(member);

        if (Boolean.TRUE.equals(request.getReceiveEmail()) && request.getCategoryIds() != null) {
            for (Long catId : request.getCategoryIds()) {
                Category category = categoryRepository.findById(catId)
                        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));

                userSubscriptionRepository.findByMemberAndCategoryId(member, catId)
                        .ifPresentOrElse(
                                existing -> existing.setIsActive(true),
                                () -> userSubscriptionRepository.save(UserSubscription.create(member, category, true))
                        );
            }
        }
    }
}
