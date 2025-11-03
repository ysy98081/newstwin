package com.est.newstwin.service;

import com.est.newstwin.domain.Member;
import com.est.newstwin.dto.member.MemberResponseDto;
import com.est.newstwin.dto.member.MemberUpdateRequestDto;
import com.est.newstwin.exception.CustomException;
import com.est.newstwin.exception.ErrorCode;
import com.est.newstwin.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 마이페이지 관련 비즈니스 로직
 * - 회원 정보 조회 및 수정
 */
@Service
@RequiredArgsConstructor
public class MypageService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /** 내 정보 조회 */
    @Transactional(readOnly = true)
    public MemberResponseDto getMyInfo(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberResponseDto.fromEntity(member);
    }

    /** 내 정보 수정 (입력된 값만 반영) */
    @Transactional(readOnly = false)
    public MemberResponseDto updateMyInfo(String email, MemberUpdateRequestDto dto) {

        System.out.println(">>> 요청 DTO = " + dto);
        System.out.println(">>> memberName = " + dto.getMemberName());
        System.out.println(">>> password = " + dto.getPassword());
        System.out.println(">>> receiveEmail = " + dto.getReceiveEmail());
        System.out.println(">>> profileImage = " + (dto.getProfileImage() != null ? dto.getProfileImage().getOriginalFilename() : null));


        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        boolean changed = false;

        // 1) 비밀번호 암호화 후 변경
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
                String fileName = UUID.randomUUID() + "_" + dto.getProfileImage().getOriginalFilename();
                Path savePath = Paths.get("uploads/profile/" + fileName);
                Files.createDirectories(savePath.getParent());
                dto.getProfileImage().transferTo(savePath.toFile());
                member.updateInfo(null, null, null, "/uploads/profile/" + fileName);
                changed = true;
            } catch (IOException e) {
                e.printStackTrace();
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        }

        // 여기서 명시적으로 저장
        if (changed) {
            Member saved = memberRepository.save(member);
            System.out.println(">>> 변경된 회원 저장 완료: " + saved.getId());
        } else {
            System.out.println(">>> 변경 없음 - 업데이트 스킵");
        }


        return MemberResponseDto.fromEntity(member);
    }
}
