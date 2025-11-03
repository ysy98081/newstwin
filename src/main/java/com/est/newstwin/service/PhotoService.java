package com.est.newstwin.service;

import com.est.newstwin.domain.Photo;
import com.est.newstwin.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;

    private final S3Client s3Client;
    private final String bucketName;
    private final String region;
    private final String activeProfile;

    public PhotoService(
            PhotoRepository photoRepository,
            @Value("${aws.access-key-id}") String accessKey,
            @Value("${aws.secret-access-key}") String secretKey,
            @Value("${aws.region}") String region,
            @Value("${aws.s3.bucket-name}") String bucketName,
            @Value("${spring.profiles.active:local}") String activeProfile
    ) {
        this.photoRepository = photoRepository;
        this.bucketName = bucketName;
        this.region = region;
        this.activeProfile = activeProfile;

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    /**
     * S3 → 실패 시 로컬로 자동 폴백
     */
    public Photo savePhoto(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        try {
            if ("local".equalsIgnoreCase(activeProfile)) {
                return savePhotoLocal(file);
            } else {
                try {
                    return savePhotoS3(file);
                } catch (Exception s3Exception) {
                    System.err.println("[S3 업로드 실패] → 로컬에 저장 시도: " + s3Exception.getMessage());
                    return savePhotoLocal(file);
                }
            }
        } catch (IOException e) {
            throw new IOException("파일 업로드 실패: " + e.getMessage());
        }
    }

    /**
     * S3 업로드
     */
    private Photo savePhotoS3(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        String uuid = UUID.randomUUID().toString();
        String newFileName = uuid + ext;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(newFileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

        String s3Url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, newFileName);

        Photo photo = Photo.builder()
                .s3Key(newFileName)
                .s3Url(s3Url)
                .originalFilename(originalFilename)
                .uploadDate(LocalDateTime.now())
                .build();

        return photoRepository.save(photo);
    }

    /**
     * 로컬 업로드
     */
    private Photo savePhotoLocal(MultipartFile file) throws IOException {
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        String uuid = UUID.randomUUID().toString();
        String newFileName = uuid + ext;

        File dest = new File(dir, newFileName);
        file.transferTo(dest);

        Photo photo = Photo.builder()
                .s3Key(newFileName)
                .s3Url("/uploads/" + newFileName)
                .originalFilename(originalFilename)
                .uploadDate(LocalDateTime.now())
                .build();

        return photoRepository.save(photo);
    }

    /**
     * S3 파일 삭제
     */
    public void deletePhotoFromS3(String s3Key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            System.err.println("[S3 파일 삭제 실패] " + e.getMessage());
        }
    }
}
