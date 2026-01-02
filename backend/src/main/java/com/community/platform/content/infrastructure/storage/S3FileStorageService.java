package com.community.platform.content.infrastructure.storage;

import com.community.platform.content.domain.AttachmentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * AWS S3 기반 파일 저장 서비스
 * S3에 파일을 업로드/삭제하고 URL을 관리
 */
@Slf4j
@Service
@ConditionalOnProperty(
    name = "community.file.storage.type",
    havingValue = "s3"
)
public class S3FileStorageService {

    private final S3Client s3Client;
    private final String bucketName;
    private final String s3Prefix;

    public S3FileStorageService(
            S3Client s3Client,
            @Value("${community.file.s3.bucket}") String bucketName,
            @Value("${community.file.s3.prefix}") String s3Prefix) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.s3Prefix = s3Prefix;
        log.info("S3FileStorageService 초기화. bucket: {}, prefix: {}", bucketName, s3Prefix);
    }

    /**
     * 파일 저장
     * @return S3 객체 키 (경로)
     */
    public String saveFile(MultipartFile file, AttachmentType type) {
        validateFile(file, type);

        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String datePath = generateDatePath();
        String s3Key = String.format("%s/%s/%s", s3Prefix, datePath, fileName);

        try {
            // S3에 파일 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("S3 파일 저장 완료. bucket: {}, key: {}", bucketName, s3Key);
            return s3Key;

        } catch (IOException e) {
            log.error("S3 파일 저장 실패. key: {}", s3Key, e);
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다", e);
        } catch (S3Exception e) {
            log.error("S3 업로드 오류. key: {}, error: {}", s3Key, e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("S3 업로드 중 오류가 발생했습니다: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String s3Key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("S3 파일 삭제 완료. bucket: {}, key: {}", bucketName, s3Key);

        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                log.warn("삭제할 S3 파일이 존재하지 않음. key: {}", s3Key);
            } else {
                log.error("S3 파일 삭제 실패. key: {}, error: {}", s3Key, e.awsErrorDetails().errorMessage(), e);
                throw new RuntimeException("S3 파일 삭제 중 오류가 발생했습니다: " + e.awsErrorDetails().errorMessage(), e);
            }
        }
    }

    /**
     * UUID 기반 고유 파일명 생성
     */
    public String generateUniqueFileName(String originalFileName) {
        String extension = extractExtension(originalFileName);
        return UUID.randomUUID().toString() + "." + extension;
    }

    /**
     * 파일 확장자 추출
     */
    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("파일 확장자를 찾을 수 없습니다");
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 날짜별 경로 생성 (예: 2025/12/23)
     */
    private String generateDatePath() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }

    /**
     * 파일 검증 (타입, 크기, 확장자)
     */
    public void validateFile(MultipartFile file, AttachmentType type) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        // 파일 크기 검증
        if (!type.isValidSize(file.getSize())) {
            throw new IllegalArgumentException(
                    String.format("%s 파일 크기는 최대 %dMB까지 허용됩니다",
                            type.getDisplayName(),
                            type.getMaxFileSize() / (1024 * 1024))
            );
        }

        // 확장자 검증
        String extension = extractExtension(file.getOriginalFilename());
        if (!type.isValidExtension(extension)) {
            throw new IllegalArgumentException(
                    String.format("지원하지 않는 파일 형식입니다. 허용 형식: %s",
                            String.join(", ", type.getAllowedExtensions()))
            );
        }
    }

    /**
     * S3 파일 URL 조회
     */
    public String getFileUrl(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, "ap-northeast-2", s3Key);
    }

    /**
     * Presigned URL 생성 (임시 다운로드 URL)
     */
    public String getPresignedUrl(String s3Key, int expirationMinutes) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            // Presigned URL 생성은 S3Presigner 사용 권장
            // 현재는 public URL 반환
            return getFileUrl(s3Key);

        } catch (Exception e) {
            log.error("Presigned URL 생성 실패. key: {}", s3Key, e);
            throw new RuntimeException("파일 URL 생성 중 오류가 발생했습니다", e);
        }
    }
}
