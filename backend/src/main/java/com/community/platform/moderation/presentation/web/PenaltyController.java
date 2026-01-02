package com.community.platform.moderation.presentation.web;

import com.community.platform.moderation.application.UserPenaltyService;
import com.community.platform.moderation.domain.UserPenalty;
import com.community.platform.moderation.dto.PenaltyCheckResponse;
import com.community.platform.moderation.dto.PenaltyCreateRequest;
import com.community.platform.moderation.dto.PenaltyResponse;
import com.community.platform.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 제재 관리 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/penalties")
@RequiredArgsConstructor
public class PenaltyController {

    private final UserPenaltyService penaltyService;

    /**
     * 제재 부여 (관리자 전용)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PenaltyResponse>> createPenalty(
            @Valid @RequestBody PenaltyCreateRequest request,
            @RequestParam(required = false) Long currentUserId) {

        log.info("제재 부여 요청. adminId: {}, targetUserId: {}, type: {}",
                currentUserId, request.getTargetUserId(), request.getPenaltyType());

        // TODO: 관리자 권한 체크 (Spring Security 통합 후)

        UserPenalty penalty = penaltyService.applyPenalty(
                request.getTargetUserId(),
                request.getPenaltyType(),
                request.getReason(),
                currentUserId
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        PenaltyResponse.from(penalty),
                        "제재가 부여되었습니다"
                ));
    }

    /**
     * 사용자의 제재 목록 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PenaltyResponse>>> getUserPenalties(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("사용자 제재 목록 조회. userId: {}", userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<UserPenalty> penaltiesPage = penaltyService.getPenaltyHistory(userId, pageable);

        List<PenaltyResponse> responses = penaltiesPage.getContent().stream()
                .map(PenaltyResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 사용자의 활성 제재 조회
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<ApiResponse<List<PenaltyResponse>>> getActivePenalties(
            @PathVariable Long userId) {

        log.info("사용자 활성 제재 조회. userId: {}", userId);

        List<UserPenalty> penalties = penaltyService.getActivePenalties(userId);

        List<PenaltyResponse> responses = penalties.stream()
                .map(PenaltyResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 활성화된 모든 제재 목록 조회 (관리자 전용)
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PenaltyResponse>>> getAllActivePenalties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("전체 활성 제재 목록 조회");

        // TODO: 관리자 권한 체크

        Pageable pageable = PageRequest.of(page, size);
        Page<UserPenalty> penaltiesPage = penaltyService.getActivePenaltiesForAdmin(pageable);

        List<PenaltyResponse> responses = penaltiesPage.getContent().stream()
                .map(PenaltyResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 제재 해제 (관리자 전용)
     */
    @DeleteMapping("/{penaltyId}")
    public ResponseEntity<ApiResponse<Void>> expirePenalty(
            @PathVariable Long penaltyId,
            @RequestParam(required = false) Long currentUserId) {

        log.info("제재 해제 요청. penaltyId: {}, adminId: {}", penaltyId, currentUserId);

        // TODO: 관리자 권한 체크

        penaltyService.expirePenalty(penaltyId);

        return ResponseEntity.ok(ApiResponse.success(null, "제재가 해제되었습니다"));
    }

    /**
     * 글쓰기 가능 여부 확인
     */
    @GetMapping("/users/{userId}/can-post")
    public ResponseEntity<ApiResponse<PenaltyCheckResponse>> canPost(
            @PathVariable Long userId) {

        log.info("글쓰기 가능 여부 확인. userId: {}", userId);

        boolean canPost = penaltyService.canCreatePost(userId);
        boolean canComment = penaltyService.canCreateComment(userId);

        PenaltyCheckResponse response = PenaltyCheckResponse.of(userId, canPost, canComment);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 댓글 작성 가능 여부 확인
     */
    @GetMapping("/users/{userId}/can-comment")
    public ResponseEntity<ApiResponse<PenaltyCheckResponse>> canComment(
            @PathVariable Long userId) {

        log.info("댓글 작성 가능 여부 확인. userId: {}", userId);

        boolean canPost = penaltyService.canCreatePost(userId);
        boolean canComment = penaltyService.canCreateComment(userId);

        PenaltyCheckResponse response = PenaltyCheckResponse.of(userId, canPost, canComment);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
