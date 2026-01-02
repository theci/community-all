package com.community.platform.content.infrastructure.storage;

import com.community.platform.content.domain.AttachmentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 파일 저장 서비스
 * 실제 파일 시스템에 파일을 저장/삭제
 */
@Slf4j
@Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
    name = "community.file.storage.type",
    havingValue = "local"
)
public class FileStorageService {

    private final String uploadBasePath;

    public FileStorageService(@Value("${community.file.upload-dir:/uploads/posts}") String uploadBasePath) {
        this.uploadBasePath = uploadBasePath;
        initializeStorage();
    }

    /**
     * 저장소 초기화 (디렉토리 생성)
     */
    private void initializeStorage() {
        try {
            Path uploadPath = Paths.get(uploadBasePath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("파일 저장소 디렉토리 생성: {}", uploadBasePath);
            } else {
                log.info("파일 저장소 디렉토리 존재 확인: {}", uploadBasePath);
            }
        } catch (IOException e) {
            // 초기화 실패 시 경고만 로깅 (파일 저장 시 재시도)
            log.warn("파일 저장소 초기화 실패 (파일 업로드 시 재생성됩니다): {}", uploadBasePath, e);
        }
    }

    /**
     * 파일 저장
     * @return 저장된 파일의 경로
     */
    public String saveFile(MultipartFile file, AttachmentType type) {
        validateFile(file, type);

        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String datePath = generateDatePath();
        Path targetPath = Paths.get(uploadBasePath, datePath);

        try {
            // 날짜별 디렉토리 생성
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
            }

            // 파일 저장
            Path filePath = targetPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("파일 저장 완료: {}", filePath);
            return datePath + "/" + fileName;

        } catch (IOException e) {
            log.error("파일 저장 실패: {}", fileName, e);
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(uploadBasePath, filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("파일 삭제 완료: {}", filePath);
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", filePath);
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", filePath, e);
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다", e);
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
     * 날짜별 경로 생성 (예: 2025/01/15)
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
     * 전체 파일 경로 조회
     */
    public Path getFullPath(String filePath) {
        return Paths.get(uploadBasePath, filePath);
    }
}
