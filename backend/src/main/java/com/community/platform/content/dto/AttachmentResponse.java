package com.community.platform.content.dto;

import com.community.platform.content.domain.AttachmentType;
import com.community.platform.content.domain.PostAttachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 첨부파일 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class AttachmentResponse {

    private Long id;
    private Long postId;
    private String fileName;
    private String originalName;
    private AttachmentType fileType;
    private String filePath;
    private String fileUrl;  // 다운로드/접근 URL
    private Long fileSize;
    private String mimeType;
    private Integer displayOrder;

    /**
     * PostAttachment 엔티티로부터 DTO 생성
     */
    public static AttachmentResponse from(PostAttachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .postId(attachment.getPostId())
                .fileName(attachment.getFileName())
                .originalName(attachment.getOriginalName())
                .fileType(attachment.getFileType())
                .filePath(attachment.getFilePath())
                .fileUrl("/api/v1/files/" + attachment.getFilePath())  // 파일 접근 URL
                .fileSize(attachment.getFileSize())
                .mimeType(attachment.getMimeType())
                .displayOrder(attachment.getDisplayOrder())
                .build();
    }
}
