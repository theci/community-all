package com.community.platform.notification.presentation.web;

import com.community.platform.notification.application.NotificationPreferenceService;
import com.community.platform.notification.application.NotificationService;
import com.community.platform.notification.domain.Notification;
import com.community.platform.notification.domain.NotificationPreference;
import com.community.platform.notification.dto.NotificationPreferenceResponse;
import com.community.platform.notification.dto.NotificationPreferenceUpdateRequest;
import com.community.platform.notification.dto.NotificationResponse;
import com.community.platform.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 알림 관리 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationPreferenceService preferenceService;

    /**
     * 내 알림 목록 조회
     * GET /api/v1/notifications
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(
            @RequestParam(required = false) Long currentUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("내 알림 목록 조회. userId: {}, page: {}", currentUserId, page);

        // TODO: Spring Security 적용 후 @AuthenticationPrincipal 사용

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getMyNotifications(currentUserId, pageable);

        List<NotificationResponse> responses = notifications.getContent().stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 읽지 않은 알림 개수 조회
     * GET /api/v1/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @RequestParam(required = false) Long currentUserId) {

        log.info("읽지 않은 알림 개수 조회. userId: {}", currentUserId);

        Long count = notificationService.getUnreadCount(currentUserId);

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * 읽지 않은 알림 목록 조회
     * GET /api/v1/notifications/unread
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            @RequestParam(required = false) Long currentUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("읽지 않은 알림 목록 조회. userId: {}", currentUserId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getUnreadNotifications(currentUserId, pageable);

        List<NotificationResponse> responses = notifications.getContent().stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 알림 읽음 처리
     * PUT /api/v1/notifications/{notificationId}/read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam(required = false) Long currentUserId) {

        log.info("알림 읽음 처리. notificationId: {}, userId: {}", notificationId, currentUserId);

        notificationService.markAsRead(notificationId, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(null, "알림이 읽음 처리되었습니다"));
    }

    /**
     * 모든 알림 읽음 처리
     * PUT /api/v1/notifications/read-all
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @RequestParam(required = false) Long currentUserId) {

        log.info("모든 알림 읽음 처리. userId: {}", currentUserId);

        notificationService.markAllAsRead(currentUserId);

        return ResponseEntity.ok(ApiResponse.success(null, "모든 알림이 읽음 처리되었습니다"));
    }

    /**
     * 알림 삭제
     * DELETE /api/v1/notifications/{notificationId}
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long notificationId,
            @RequestParam(required = false) Long currentUserId) {

        log.info("알림 삭제. notificationId: {}, userId: {}", notificationId, currentUserId);

        notificationService.deleteNotification(notificationId, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(null, "알림이 삭제되었습니다"));
    }

    /**
     * 알림 설정 조회
     * GET /api/v1/notifications/preferences
     */
    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPreferences(
            @RequestParam(required = false) Long currentUserId) {

        log.info("알림 설정 조회. userId: {}", currentUserId);

        NotificationPreference preference = preferenceService.getPreference(currentUserId);
        NotificationPreferenceResponse response = NotificationPreferenceResponse.from(preference);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 알림 설정 변경
     * PUT /api/v1/notifications/preferences
     */
    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updatePreferences(
            @RequestParam(required = false) Long currentUserId,
            @RequestBody NotificationPreferenceUpdateRequest request) {

        log.info("알림 설정 변경. userId: {}", currentUserId);

        NotificationPreference preference = preferenceService.updatePreference(
                currentUserId,
                request.getNotifyComment(),
                request.getNotifyReply(),
                request.getNotifyLike(),
                request.getNotifyScrap(),
                request.getNotifyReportResult(),
                request.getNotifyLevelUp(),
                request.getNotifyPenalty()
        );

        NotificationPreferenceResponse response = NotificationPreferenceResponse.from(preference);

        return ResponseEntity.ok(ApiResponse.success(response, "알림 설정이 변경되었습니다"));
    }
}
