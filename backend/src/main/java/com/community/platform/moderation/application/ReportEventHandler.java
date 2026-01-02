package com.community.platform.moderation.application;

import com.community.platform.moderation.domain.PenaltyType;
import com.community.platform.moderation.domain.ReportApprovedEvent;
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

    /**
     * 신고 승인 시 처리
     * - 신고 누적 횟수에 따라 자동 제재
     * - 고위험 신고 시 즉시 조치
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReportApproved(ReportApprovedEvent event) {
        log.info("신고 승인 이벤트 처리. reportId: {}, reportedUserId: {}, reason: {}",
                event.getReportId(), event.getReportedUserId(), event.getReason());

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
}
