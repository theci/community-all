package com.community.platform.moderation.application;

import com.community.platform.content.domain.Comment;
import com.community.platform.content.domain.CommentStatus;
import com.community.platform.content.domain.Post;
import com.community.platform.content.infrastructure.persistence.CommentRepository;
import com.community.platform.content.infrastructure.persistence.PostRepository;
import com.community.platform.moderation.domain.PenaltyType;
import com.community.platform.moderation.domain.ReportApprovedEvent;
import com.community.platform.moderation.domain.ReportTargetType;
import com.community.platform.moderation.infrastructure.persistence.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * 신고 이벤트 핸들러
 * 신고 승인 시 자동 제재 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportEventHandler {

    private final ReportRepository reportRepository;
    private final UserPenaltyService penaltyService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /**
     * 신고 승인 시 처리
     * - 신고된 콘텐츠 삭제/상태 변경 (actionTaken에 따라 결정)
     * - 신고 누적 횟수에 따라 자동 제재
     * - 고위험 신고 시 즉시 조치
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void handleReportApproved(ReportApprovedEvent event) {
        log.info("신고 승인 이벤트 처리. reportId: {}, reportedUserId: {}, reason: {}, actionTaken: {}",
                event.getReportId(), event.getReportedUserId(), event.getReason(), event.getActionTaken());

        // 0. 신고된 콘텐츠 처리 (actionTaken 값에 따라)
        if (shouldDeleteContent(event.getActionTaken())) {
            handleReportedContent(event.getTargetType(), event.getTargetId());
        } else {
            log.info("콘텐츠 삭제 없이 제재만 적용. actionTaken: {}", event.getActionTaken());
        }

        Long reportedUserId = event.getReportedUserId();

        // 1. 최근 3개월 내 신고 승인 횟수 조회
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        Long approvedCount = reportRepository.countApprovedReportsByUser(
                reportedUserId, threeMonthsAgo
        );

        log.info("최근 3개월 신고 승인 횟수. userId: {}, count: {}", reportedUserId, approvedCount);

        // 2. 신고 누적 횟수에 따른 자동 제재
        if (approvedCount >= 5) {
            // 5회 이상: 7일 글쓰기 금지
            applyAutoPenalty(
                    reportedUserId,
                    PenaltyType.POST_BAN_7D,
                    String.format("신고 누적 %d회 (최근 3개월)", approvedCount)
            );
        } else if (approvedCount >= 3) {
            // 3회 이상: 24시간 글쓰기 금지
            applyAutoPenalty(
                    reportedUserId,
                    PenaltyType.POST_BAN_24H,
                    String.format("신고 누적 %d회 (최근 3개월)", approvedCount)
            );
        }

        // 3. 고위험 신고인 경우 즉시 조치
        if (event.getReason().isHighSeverity()) {
            log.warn("고위험 신고 감지. userId: {}, reason: {}",
                    reportedUserId, event.getReason());

            applyAutoPenalty(
                    reportedUserId,
                    PenaltyType.FULL_BAN,
                    String.format("고위험 신고 승인 - %s", event.getReason().getDisplayName())
            );
        }
    }

    /**
     * 신고된 콘텐츠 처리
     */
    private void handleReportedContent(ReportTargetType targetType, Long targetId) {
        try {
            switch (targetType) {
                case POST -> {
                    // 게시글 삭제 (소프트 삭제)
                    postRepository.findById(targetId).ifPresent(post -> {
                        post.delete(null); // 시스템에 의한 삭제
                        postRepository.save(post);
                        log.info("신고된 게시글 삭제 완료. postId: {}", targetId);
                    });
                }
                case COMMENT -> {
                    // 댓글 삭제 (소프트 삭제)
                    commentRepository.findById(targetId).ifPresent(comment -> {
                        comment.delete(); // DELETED 상태로 변경 및 게시글 댓글 수 감소
                        commentRepository.save(comment);
                        log.info("신고된 댓글 삭제 완료. commentId: {}", targetId);
                    });
                }
                case USER -> {
                    // 사용자 신고는 제재만 적용 (계정 삭제는 하지 않음)
                    log.info("사용자 신고는 제재만 적용됩니다. userId: {}", targetId);
                }
                case CHAT -> {
                    // TODO: 채팅 기능 구현 후 추가
                    log.warn("채팅 신고 처리는 아직 구현되지 않았습니다.");
                }
            }
        } catch (Exception e) {
            log.error("신고된 콘텐츠 처리 실패. targetType: {}, targetId: {}", targetType, targetId, e);
            // 콘텐츠 처리 실패해도 제재는 계속 진행
        }
    }

    /**
     * 자동 제재 부여
     * 중복 제재 방지 로직 포함
     */
    private void applyAutoPenalty(Long userId, PenaltyType penaltyType, String reason) {
        try {
            // 이미 동일한 타입의 제재가 있는지 확인
            if (penaltyService.hasActivePenalty(userId, penaltyType)) {
                log.info("이미 동일한 제재가 적용되어 있습니다. userId: {}, type: {}",
                        userId, penaltyType);
                return;
            }

            // 제재 부여 (adminId = null: 자동 제재)
            penaltyService.applyPenalty(userId, penaltyType, reason, null);
            log.info("자동 제재 부여 완료. userId: {}, type: {}, reason: {}",
                    userId, penaltyType, reason);

        } catch (Exception e) {
            log.error("자동 제재 부여 실패. userId: {}, type: {}", userId, penaltyType, e);
            // 제재 부여 실패해도 신고 처리는 계속 진행
        }
    }

    /**
     * actionTaken 값에 따라 콘텐츠 삭제 여부 결정
     * @param actionTaken 관리자가 선택한 조치 내용
     * @return 콘텐츠를 삭제해야 하면 true
     */
    private boolean shouldDeleteContent(String actionTaken) {
        if (actionTaken == null) {
            return false;
        }

        // "삭제", "차단", "콘텐츠 삭제" 등의 키워드가 있으면 삭제
        String lowerAction = actionTaken.toLowerCase();
        return lowerAction.contains("삭제") ||
               lowerAction.contains("차단") ||
               lowerAction.contains("delete") ||
               lowerAction.contains("block") ||
               lowerAction.contains("remove");
    }
}
