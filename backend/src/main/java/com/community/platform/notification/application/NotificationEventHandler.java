package com.community.platform.notification.application;

import com.community.platform.content.domain.CommentCreatedEvent;
import com.community.platform.engagement.domain.LikeCreatedEvent;
import com.community.platform.engagement.domain.ScrapCreatedEvent;
import com.community.platform.messaging.domain.MessageCreatedEvent;
import com.community.platform.moderation.domain.ReportApprovedEvent;
import com.community.platform.moderation.domain.ReportRejectedEvent;
import com.community.platform.moderation.domain.UserPenaltyCreatedEvent;
import com.community.platform.moderation.domain.UserPenaltyExpiredEvent;
import com.community.platform.notification.domain.NotificationType;
import com.community.platform.notification.domain.RelatedEntityType;
import com.community.platform.reward.domain.UserLevelUpEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 알림 이벤트 핸들러
 * 도메인 이벤트를 수신하여 알림 발송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationService notificationService;

    /**
     * 댓글 작성 이벤트 처리
     * - 게시글에 댓글: 게시글 작성자에게 알림
     * - 대댓글: 부모 댓글 작성자에게 알림
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreated(CommentCreatedEvent event) {
        log.debug("댓글 생성 이벤트 수신. commentId: {}, postId: {}", event.getCommentId(), event.getPostId());

        // 자기 자신에게는 알림 발송 안 함
        if (event.getParentCommentId() == null) {
            // 게시글에 댓글 작성
            if (!event.getCommentAuthorId().equals(event.getPostAuthorId())) {
                notificationService.sendNotification(
                        event.getPostAuthorId(),
                        NotificationType.COMMENT_ON_POST,
                        "새 댓글",
                        "회원님의 게시글에 댓글이 달렸습니다",
                        event.getCommentId(),
                        RelatedEntityType.COMMENT
                );
            }
        } else {
            // 대댓글 작성
            if (!event.getCommentAuthorId().equals(event.getParentCommentAuthorId())) {
                notificationService.sendNotification(
                        event.getParentCommentAuthorId(),
                        NotificationType.REPLY_ON_COMMENT,
                        "댓글에 답글",
                        "회원님의 댓글에 답글이 달렸습니다",
                        event.getCommentId(),
                        RelatedEntityType.COMMENT
                );
            }
        }
    }

    /**
     * 좋아요 생성 이벤트 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLikeCreated(LikeCreatedEvent event) {
        log.debug("좋아요 생성 이벤트 수신. likeId: {}, postId: {}", event.getLikeId(), event.getPostId());

        // 자기 자신에게는 알림 발송 안 함
        if (!event.getUserId().equals(event.getPostAuthorId())) {
            notificationService.sendNotification(
                    event.getPostAuthorId(),
                    NotificationType.LIKE_ON_POST,
                    "좋아요",
                    "회원님의 게시글을 좋아합니다",
                    event.getPostId(),
                    RelatedEntityType.POST
            );
        }
    }

    /**
     * 스크랩 생성 이벤트 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScrapCreated(ScrapCreatedEvent event) {
        log.debug("스크랩 생성 이벤트 수신. scrapId: {}, postId: {}", event.getScrapId(), event.getPostId());

        // 자기 자신에게는 알림 발송 안 함
        if (!event.getUserId().equals(event.getPostAuthorId())) {
            notificationService.sendNotification(
                    event.getPostAuthorId(),
                    NotificationType.SCRAP_ON_POST,
                    "스크랩",
                    "회원님의 게시글을 스크랩했습니다",
                    event.getPostId(),
                    RelatedEntityType.POST
            );
        }
    }

    /**
     * 신고 승인 이벤트 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReportApproved(ReportApprovedEvent event) {
        log.debug("신고 승인 이벤트 수신. reportId: {}", event.getReportId());

        notificationService.sendNotification(
                event.getReporterId(),
                NotificationType.REPORT_APPROVED,
                "신고 승인",
                "신고하신 콘텐츠가 삭제되었습니다",
                event.getReportId(),
                RelatedEntityType.REPORT
        );
    }

    /**
     * 신고 반려 이벤트 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReportRejected(ReportRejectedEvent event) {
        log.debug("신고 반려 이벤트 수신. reportId: {}", event.getReportId());

        notificationService.sendNotification(
                event.getReporterId(),
                NotificationType.REPORT_REJECTED,
                "신고 반려",
                "신고가 반려되었습니다: " + event.getRejectReason(),
                event.getReportId(),
                RelatedEntityType.REPORT
        );
    }

    /**
     * 사용자 제재 생성 이벤트 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserPenaltyCreated(UserPenaltyCreatedEvent event) {
        log.debug("사용자 제재 생성 이벤트 수신. penaltyId: {}, userId: {}", event.getPenaltyId(), event.getUserId());

        notificationService.sendNotification(
                event.getUserId(),
                NotificationType.PENALTY_APPLIED,
                "제재 통보",
                String.format("커뮤니티 규칙 위반으로 %s 제재되었습니다", event.getPenaltyType().getDisplayName()),
                event.getPenaltyId(),
                RelatedEntityType.PENALTY
        );
    }

    /**
     * 사용자 제재 만료 이벤트 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserPenaltyExpired(UserPenaltyExpiredEvent event) {
        log.debug("사용자 제재 만료 이벤트 수신. penaltyId: {}, userId: {}", event.getPenaltyId(), event.getUserId());

        notificationService.sendNotification(
                event.getUserId(),
                NotificationType.PENALTY_EXPIRED,
                "제재 해제",
                "제재가 해제되었습니다. 정상적인 활동이 가능합니다",
                event.getPenaltyId(),
                RelatedEntityType.PENALTY
        );
    }

    /**
     * 사용자 레벨업 이벤트 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserLevelUp(UserLevelUpEvent event) {
        log.debug("사용자 레벨업 이벤트 수신. userId: {}, newLevel: {}", event.getUserId(), event.getNewLevel());

        notificationService.sendNotification(
                event.getUserId(),
                NotificationType.LEVEL_UP,
                "레벨 업!",
                String.format("축하합니다! %s이(가) 되셨습니다 (포인트: %d)",
                        event.getNewLevel().getDisplayName(),
                        event.getTotalPoints()),
                event.getUserId(),
                RelatedEntityType.USER
        );
    }

    /**
     * 쪽지 생성 이벤트 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageCreated(MessageCreatedEvent event) {
        log.debug("쪽지 생성 이벤트 수신. messageId: {}, recipientId: {}", event.getMessageId(), event.getRecipientId());

        notificationService.sendNotification(
                event.getRecipientId(),
                NotificationType.MESSAGE_RECEIVED,
                "새 쪽지",
                "새로운 쪽지가 도착했습니다",
                event.getMessageId(),
                RelatedEntityType.MESSAGE
        );
    }
}
