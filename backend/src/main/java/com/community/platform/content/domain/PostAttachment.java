package com.community.platform.content.domain;

import com.community.platform.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글 첨부파일 엔티티
 * 이미지 및 동영상 파일 첨부 지원
 */
@Entity
@Table(name = "post_attachments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostAttachment extends AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false, length = 255)
    private String fileName;  // UUID 생성 파일명

    @Column(nullable = false, length = 255)
    private String originalName;  // 사용자 업로드 시 원본 파일명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AttachmentType fileType;

    @Column(nullable = false, length = 500)
    private String filePath;  // 저장 경로

    @Column(nullable = false)
    private Long fileSize;  // 바이트 단위

    @Column(length = 100)
    private String mimeType;  // MIME 타입 (예: image/jpeg)

    @Column(nullable = false)
    private Integer displayOrder = 0;  // 표시 순서

    /**
     * 첨부파일 생성
     */
    public static PostAttachment create(
            Long postId,
            String fileName,
            String originalName,
            AttachmentType fileType,
            String filePath,
            Long fileSize,
            String mimeType,
            Integer displayOrder
    ) {
        PostAttachment attachment = new PostAttachment();
        attachment.postId = postId;
        attachment.fileName = fileName;
        attachment.originalName = originalName;
        attachment.fileType = fileType;
        attachment.filePath = filePath;
        attachment.fileSize = fileSize;
        attachment.mimeType = mimeType;
        attachment.displayOrder = displayOrder != null ? displayOrder : 0;

        // 파일 크기 검증
        attachment.validateSize();

        attachment.addDomainEvent(new PostAttachmentCreatedEvent(
                attachment.id,
                postId,
                fileType,
                originalName
        ));

        return attachment;
    }

    /**
     * 파일 크기 검증
     */
    public void validateSize() {
        if (!fileType.isValidSize(fileSize)) {
            throw new IllegalArgumentException(
                    String.format("%s 파일 크기는 최대 %dMB까지 허용됩니다",
                            fileType.getDisplayName(),
                            fileType.getMaxFileSize() / (1024 * 1024))
            );
        }
    }

    /**
     * 표시 순서 변경
     */
    public void updateDisplayOrder(Integer order) {
        this.displayOrder = order;
    }
}
