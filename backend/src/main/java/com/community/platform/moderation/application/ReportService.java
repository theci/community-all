package com.community.platform.moderation.application;

import com.community.platform.content.exception.CommentNotFoundException;
import com.community.platform.content.exception.PostNotFoundException;
import com.community.platform.content.infrastructure.persistence.CommentRepository;
import com.community.platform.content.infrastructure.persistence.PostRepository;
import com.community.platform.moderation.domain.*;
import com.community.platform.moderation.dto.*;
import com.community.platform.moderation.exception.DuplicateReportException;
import com.community.platform.moderation.exception.ReportNotFoundException;
import com.community.platform.moderation.infrastructure.persistence.ReportRepository;
import com.community.platform.user.exception.UserNotFoundException;
import com.community.platform.user.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 신고 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /**
     * 신고 생성
     */
    @Transactional
    public ReportResponse createReport(Long reporterId, ReportCreateRequest request) {
        log.info("신고 생성. reporterId: {}, targetType: {}, targetId: {}",
                reporterId, request.getTargetType(), request.getTargetId());

        // 신고자 존재 확인
        if (!userRepository.existsById(reporterId)) {
            throw new UserNotFoundException(reporterId);
        }

        // 중복 신고 확인
        reportRepository.findByReporterAndTarget(
            reporterId, request.getTargetType(), request.getTargetId()
        ).ifPresent(report -> {
            throw new DuplicateReportException(reporterId, request.getTargetId());
        });

        // 신고 대상 존재 확인 및 작성자 ID 조회
        Long reportedUserId = getTargetAuthorId(request.getTargetType(), request.getTargetId());

        // 신고 생성
        Report report = Report.create(
            reporterId,
            reportedUserId,
            request.getTargetType(),
            request.getTargetId(),
            request.getReason(),
            request.getDescription()
        );

        Report savedReport = reportRepository.save(report);

        log.info("신고 생성 완료. reportId: {}, reason: {}", savedReport.getId(), request.getReason());

        return ReportResponse.from(savedReport);
    }

    /**
     * 신고 조회
     */
    public ReportResponse getReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ReportNotFoundException(reportId));
        return ReportResponse.from(report);
    }

    /**
     * 상태별 신고 목록 조회
     */
    public Page<ReportResponse> getReportsByStatus(ReportStatus status, Pageable pageable) {
        return reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
            .map(ReportResponse::from);
    }

    /**
     * 대기 중인 신고 목록 조회
     */
    public Page<ReportResponse> getPendingReports(Pageable pageable) {
        return reportRepository.findPendingReports(pageable)
            .map(ReportResponse::from);
    }

    /**
     * 고위험 신고 목록 조회
     */
    public Page<ReportResponse> getHighSeverityReports(Pageable pageable) {
        return reportRepository.findHighSeverityReports(pageable)
            .map(ReportResponse::from);
    }

    /**
     * 내가 신고한 목록 조회
     */
    public Page<ReportResponse> getMyReports(Long reporterId, Pageable pageable) {
        return reportRepository.findByReporterIdOrderByCreatedAtDesc(reporterId, pageable)
            .map(ReportResponse::from);
    }

    /**
     * 특정 사용자에 대한 신고 목록 조회
     */
    public Page<ReportResponse> getReportsByUser(Long reportedUserId, Pageable pageable) {
        return reportRepository.findByReportedUserIdOrderByCreatedAtDesc(reportedUserId, pageable)
            .map(ReportResponse::from);
    }

    /**
     * 대상 유형별 신고 목록 조회
     */
    public Page<ReportResponse> getReportsByTargetType(ReportTargetType targetType, Pageable pageable) {
        return reportRepository.findByTargetTypeOrderByCreatedAtDesc(targetType, pageable)
            .map(ReportResponse::from);
    }

    /**
     * 특정 대상에 대한 신고 목록 조회
     */
    public List<ReportResponse> getReportsByTarget(ReportTargetType targetType, Long targetId) {
        return reportRepository.findByTarget(targetType, targetId).stream()
            .map(ReportResponse::from)
            .toList();
    }

    /**
     * 신고 검토 시작
     */
    @Transactional
    public ReportResponse startReview(Long reportId, Long reviewerId) {
        log.info("신고 검토 시작. reportId: {}, reviewerId: {}", reportId, reviewerId);

        // 검토자 존재 확인
        if (!userRepository.existsById(reviewerId)) {
            throw new UserNotFoundException(reviewerId);
        }

        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ReportNotFoundException(reportId));

        report.startReview(reviewerId);
        Report savedReport = reportRepository.save(report);

        log.info("신고 검토 시작 완료. reportId: {}", reportId);

        return ReportResponse.from(savedReport);
    }

    /**
     * 신고 승인
     */
    @Transactional
    public ReportResponse approveReport(Long reportId, Long reviewerId, ReportReviewRequest request) {
        log.info("신고 승인. reportId: {}, reviewerId: {}", reportId, reviewerId);

        // 검토자 존재 확인
        if (!userRepository.existsById(reviewerId)) {
            throw new UserNotFoundException(reviewerId);
        }

        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ReportNotFoundException(reportId));

        report.approve(reviewerId, request.getReviewComment(), request.getActionTaken());
        Report savedReport = reportRepository.save(report);

        log.info("신고 승인 완료. reportId: {}, actionTaken: {}", reportId, request.getActionTaken());

        return ReportResponse.from(savedReport);
    }

    /**
     * 신고 반려
     */
    @Transactional
    public ReportResponse rejectReport(Long reportId, Long reviewerId, ReportReviewRequest request) {
        log.info("신고 반려. reportId: {}, reviewerId: {}", reportId, reviewerId);

        // 검토자 존재 확인
        if (!userRepository.existsById(reviewerId)) {
            throw new UserNotFoundException(reviewerId);
        }

        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ReportNotFoundException(reportId));

        report.reject(reviewerId, request.getReviewComment());
        Report savedReport = reportRepository.save(report);

        log.info("신고 반려 완료. reportId: {}", reportId);

        return ReportResponse.from(savedReport);
    }

    /**
     * 신고 통계 조회
     */
    public ReportStatisticsResponse getReportStatistics() {
        Long totalReports = reportRepository.count();
        Long pendingReports = reportRepository.countByStatus(ReportStatus.PENDING);
        Long inReviewReports = reportRepository.countByStatus(ReportStatus.IN_REVIEW);
        Long approvedReports = reportRepository.countByStatus(ReportStatus.APPROVED);
        Long rejectedReports = reportRepository.countByStatus(ReportStatus.REJECTED);

        // 고위험 신고 수 계산 (PENDING 상태이면서 심각도가 높은 신고)
        Page<Report> highSeverityPage = reportRepository.findHighSeverityReports(Pageable.ofSize(1));
        Long highSeverityReports = highSeverityPage.getTotalElements();

        return ReportStatisticsResponse.builder()
            .totalReports(totalReports)
            .pendingReports(pendingReports)
            .inReviewReports(inReviewReports)
            .approvedReports(approvedReports)
            .rejectedReports(rejectedReports)
            .highSeverityReports(highSeverityReports)
            .build();
    }

    /**
     * 기간별 신고 수 조회
     */
    public Long getReportCountByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return reportRepository.countByDateRange(startDate, endDate);
    }

    /**
     * 특정 대상의 신고 수 조회
     */
    public Long getReportCountByTarget(ReportTargetType targetType, Long targetId) {
        return reportRepository.countByTarget(targetType, targetId);
    }

    // Helper methods

    /**
     * 신고 대상의 작성자 ID 조회
     */
    private Long getTargetAuthorId(ReportTargetType targetType, Long targetId) {
        return switch (targetType) {
            case POST -> {
                var post = postRepository.findById(targetId)
                    .orElseThrow(() -> new PostNotFoundException(targetId));
                yield post.getAuthorId();
            }
            case COMMENT -> {
                var comment = commentRepository.findById(targetId)
                    .orElseThrow(() -> new CommentNotFoundException(targetId));
                yield comment.getAuthorId();
            }
            case USER -> {
                if (!userRepository.existsById(targetId)) {
                    throw new UserNotFoundException(targetId);
                }
                yield targetId; // 사용자 자체를 신고하는 경우
            }
            case CHAT -> {
                // TODO: 채팅 기능 구현 후 추가
                log.warn("채팅 신고는 아직 지원하지 않습니다.");
                yield null;
            }
        };
    }
}
