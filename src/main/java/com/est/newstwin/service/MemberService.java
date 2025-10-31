package com.est.newstwin.service;

import com.est.newstwin.domain.Member;
import com.est.newstwin.dto.member.MemberRequestDto;
import com.est.newstwin.dto.member.MemberResponseDto;
import com.est.newstwin.exception.CustomException;
import com.est.newstwin.exception.ErrorCode;
import com.est.newstwin.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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
                Member.Role.ROLE_USER
        );

        // 저장 및 응답 DTO 변환
        Member savedMember = memberRepository.save(member);

        return MemberResponseDto.fromEntity(savedMember);
    }
}
