package com.est.newstwin.service;

import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class ProfileImageService {

  private final S3Client s3Client;
  private final String bucketName;
  private final String region;

  public ProfileImageService(
      @Value("${aws.access-key-id}") String accessKey,
      @Value("${aws.secret-access-key}") String secretKey,
      @Value("${aws.region}") String region,
      @Value("${aws.s3.bucket-name}") String bucketName
  ) {
    this.bucketName = bucketName;
    this.region = region;

    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

    this.s3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();
  }


  // 임시 TEMP 업로드
  public String uploadTemp(MultipartFile file, Long memberId) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("빈 파일입니다.");
    }

    String ext = getExt(file.getOriginalFilename());
    if (ext.isBlank()) ext = ".png";

    String key = "temp/profile/" + memberId + "_" + UUID.randomUUID() + ext;

    PutObjectRequest req = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .contentType(file.getContentType())
        .build();

    s3Client.putObject(req, RequestBody.fromBytes(file.getBytes()));

    return toUrl(key);
  }

  // 확정시 TEMP → FINAL
  public String finalizeProfile(String tempUrlOrKey, String oldProfileUrl) {

    if (tempUrlOrKey == null || tempUrlOrKey.isBlank()) {
      return oldProfileUrl;
    }

    String tempKey = toKey(tempUrlOrKey);
    String fileName = tempKey.substring(tempKey.lastIndexOf("/") + 1);
    String finalKey = "profile/" + fileName;

    // 1) temp → final copy
    CopyObjectRequest copyReq = CopyObjectRequest.builder()
        .sourceBucket(bucketName)
        .sourceKey(tempKey)
        .destinationBucket(bucketName)
        .destinationKey(finalKey)
        .build();

    s3Client.copyObject(copyReq);

    // 2) temp 삭제
    deleteByKey(tempKey);

    // 3) old 삭제
    if (oldProfileUrl != null && oldProfileUrl.contains("amazonaws.com")) {
      String oldKey = toKey(oldProfileUrl);
      deleteByKey(oldKey);
    }

    return toUrl(finalKey);
  }

  // DELETE
  public void deleteByKey(String key) {
    try {
      DeleteObjectRequest req = DeleteObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();
      s3Client.deleteObject(req);
    } catch (Exception e) {
      System.err.println("[ProfileImageService] 삭제 실패: key=" + key + ", msg=" + e.getMessage());
    }
  }

  // UTIL
  private String getExt(String originalFilename) {
    if (originalFilename == null) return "";
    int idx = originalFilename.lastIndexOf(".");
    return idx != -1 ? originalFilename.substring(idx) : "";
  }

  private String toUrl(String key) {
    return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
  }

  public String toKey(String urlOrKey) {
    if (urlOrKey.contains(".amazonaws.com/")) {
      return urlOrKey.substring(urlOrKey.indexOf(".amazonaws.com/") + ".amazonaws.com/".length());
    }
    return urlOrKey;
  }
}