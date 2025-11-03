package com.est.newstwin.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "photo")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 저장용 key (UUID + 확장자)
    @Column(nullable = false)
    private String s3Key;

    // 실제 접근 가능한 URL
    @Column(nullable = false)
    private String s3Url;

    // 사용자가 업로드한 원본 파일명
    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private LocalDateTime uploadDate;
}
