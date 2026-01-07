package com.community.platform.messaging.presentation.web;

import com.community.platform.messaging.application.MessageService;
import com.community.platform.messaging.dto.MessageResponse;
import com.community.platform.messaging.dto.MessageSendRequest;
import com.community.platform.messaging.dto.MessageThreadResponse;
import com.community.platform.shared.dto.ApiResponse;
import com.community.platform.shared.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 메시지 관리 REST API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 메시지 전송
     * POST /api/v1/messages
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MessageResponse> sendMessage(
            @RequestParam Long currentUserId, // TODO: Security 적용 후 제거
            @Valid @RequestBody MessageSendRequest request) {
        log.info("메시지 전송: senderId={}, recipientId={}", currentUserId, request.getRecipientId());

        MessageResponse response = messageService.sendMessage(
            currentUserId,
            request.getRecipientId(),
            request.getContent()
        );

        return ApiResponse.success(response, "메시지가 전송되었습니다");
    }

    /**
     * 메시지 조회
     * GET /api/v1/messages/{messageId}
     */
    @GetMapping("/{messageId}")
    public ApiResponse<MessageResponse> getMessage(
            @PathVariable Long messageId,
            @RequestParam Long currentUserId) {
        log.info("메시지 조회: messageId={}, currentUserId={}", messageId, currentUserId);

        MessageResponse response = messageService.getMessage(messageId, currentUserId);
        return ApiResponse.success(response);
    }

    /**
     * 받은 메시지 목록 조회
     * GET /api/v1/messages/inbox
     */
    @GetMapping("/inbox")
    public ApiResponse<PageResponse<MessageResponse>> getInbox(
            @RequestParam Long currentUserId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("받은 메시지 목록 조회: userId={}", currentUserId);

        Page<MessageResponse> messages = messageService.getReceivedMessages(currentUserId, pageable);
        return ApiResponse.success(PageResponse.of(messages));
    }

    /**
     * 보낸 메시지 목록 조회
     * GET /api/v1/messages/sent
     */
    @GetMapping("/sent")
    public ApiResponse<PageResponse<MessageResponse>> getSentMessages(
            @RequestParam Long currentUserId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("보낸 메시지 목록 조회: userId={}", currentUserId);

        Page<MessageResponse> messages = messageService.getSentMessages(currentUserId, pageable);
        return ApiResponse.success(PageResponse.of(messages));
    }

    /**
     * 대화 스레드 목록 조회
     * GET /api/v1/messages/threads
     */
    @GetMapping("/threads")
    public ApiResponse<PageResponse<MessageThreadResponse>> getThreads(
            @RequestParam Long currentUserId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("대화 스레드 목록 조회: userId={}", currentUserId);

        Page<MessageThreadResponse> threads = messageService.getThreads(currentUserId, pageable);
        return ApiResponse.success(PageResponse.of(threads));
    }

    /**
     * 특정 스레드의 메시지 목록 조회
     * GET /api/v1/messages/threads/{threadId}
     */
    @GetMapping("/threads/{threadId}")
    public ApiResponse<PageResponse<MessageResponse>> getThreadMessages(
            @PathVariable Long threadId,
            @RequestParam Long currentUserId,
            @PageableDefault(size = 50) Pageable pageable) {
        log.info("스레드 메시지 조회: threadId={}, userId={}", threadId, currentUserId);

        Page<MessageResponse> messages = messageService.getMessagesByThread(
            threadId, currentUserId, pageable);
        return ApiResponse.success(PageResponse.of(messages));
    }

    /**
     * 메시지 읽음 처리
     * PUT /api/v1/messages/{messageId}/read
     */
    @PutMapping("/{messageId}/read")
    public ApiResponse<Void> markAsRead(
            @PathVariable Long messageId,
            @RequestParam Long currentUserId) {
        log.info("메시지 읽음 처리: messageId={}, userId={}", messageId, currentUserId);

        messageService.markAsRead(messageId, currentUserId);
        return ApiResponse.success(null, "메시지를 읽음으로 표시했습니다");
    }

    /**
     * 스레드 전체 읽음 처리
     * PUT /api/v1/messages/threads/{threadId}/read
     */
    @PutMapping("/threads/{threadId}/read")
    public ApiResponse<Void> markThreadAsRead(
            @PathVariable Long threadId,
            @RequestParam Long currentUserId) {
        log.info("스레드 전체 읽음 처리: threadId={}, userId={}", threadId, currentUserId);

        messageService.markThreadAsRead(threadId, currentUserId);
        return ApiResponse.success(null, "대화를 읽음으로 표시했습니다");
    }

    /**
     * 메시지 삭제
     * DELETE /api/v1/messages/{messageId}
     */
    @DeleteMapping("/{messageId}")
    public ApiResponse<Void> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam Long currentUserId) {
        log.info("메시지 삭제: messageId={}, userId={}", messageId, currentUserId);

        messageService.deleteMessage(messageId, currentUserId);
        return ApiResponse.success(null, "메시지가 삭제되었습니다");
    }

    /**
     * 안 읽은 메시지 수 조회
     * GET /api/v1/messages/unread-count
     */
    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(@RequestParam Long currentUserId) {
        log.info("안 읽은 메시지 수 조회: userId={}", currentUserId);

        Long count = messageService.getUnreadCount(currentUserId);
        return ApiResponse.success(count);
    }
}
