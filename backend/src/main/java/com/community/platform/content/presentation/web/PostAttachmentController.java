package com.community.platform.content.presentation.web;

import com.community.platform.content.application.PostAttachmentService;
import com.community.platform.content.domain.PostAttachment;
import com.community.platform.content.dto.AttachmentResponse;
import com.community.platform.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 게시글 첨부파일 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostAttachmentController {

    private final PostAttachmentService attachmentService;

    /**
     * 첨부파일 업로드 (다중 파일 지원)
     */
    @PostMapping("/{postId}/attachments")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> uploadAttachments(
            @PathVariable Long postId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(required = false) Long currentUserId) {

        log.info("첨부파일 업로드 요청. postId: {}, fileCount: {}, userId: {}",
                postId, files.size(), currentUserId);

        // TODO: 게시글 작성자 권한 체크 (Spring Security 통합 후)

        List<PostAttachment> attachments = attachmentService.uploadAttachments(postId, files);

        List<AttachmentResponse> responses = attachments.stream()
                .map(AttachmentResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responses, "파일이 업로드되었습니다"));
    }

    /**
     * 게시글의 첨부파일 목록 조회
     */
    @GetMapping("/{postId}/attachments")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAttachments(
            @PathVariable Long postId) {

        log.info("첨부파일 목록 조회. postId: {}", postId);

        List<PostAttachment> attachments = attachmentService.getPostAttachments(postId);

        List<AttachmentResponse> responses = attachments.stream()
                .map(AttachmentResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 첨부파일 삭제
     */
    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable Long attachmentId,
            @RequestParam(required = false) Long currentUserId) {

        log.info("첨부파일 삭제 요청. attachmentId: {}, userId: {}", attachmentId, currentUserId);

        // TODO: 게시글 작성자 권한 체크 (Spring Security 통합 후)
        // 현재는 서비스 내부에서 작성자 검증

        attachmentService.deleteAttachment(attachmentId, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(null, "첨부파일이 삭제되었습니다"));
    }
}
