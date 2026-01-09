package com.community.platform.moderation.application;

import com.community.platform.content.domain.Comment;
import com.community.platform.content.domain.Post;
import com.community.platform.content.exception.CommentNotFoundException;
import com.community.platform.content.exception.PostNotFoundException;
import com.community.platform.content.infrastructure.persistence.CommentRepository;
import com.community.platform.content.infrastructure.persistence.PostRepository;
import com.community.platform.moderation.domain.*;
import com.community.platform.moderation.dto.*;
import com.community.platform.moderation.exception.DuplicateReportException;
import com.community.platform.moderation.exception.ReportNotFoundException;
import com.community.platform.moderation.infrastructure.persistence.ReportRepository;
import com.community.platform.user.domain.User;
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
    private final com.community.platform.shared.infrastructure.DomainEventPublisher eventPublisher;

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

        // 공지사항 및 관리자 콘텐츠 신고 방지
        validateReportTarget(request.getTargetType(), request.getTargetId(), reportedUserId);

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

        // 도메인 이벤트 발행 (ReportCreatedEvent)
        eventPublisher.publishEvents(savedReport);

        log.info("신고 생성 완료. reportId: {}, reason: {}", savedReport.getId(), request.getReason());

        return toReportResponse(savedReport);
    }

    /**
     * 신고 조회
     */
    public ReportResponse getReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ReportNotFoundException(reportId));
        return toReportResponse(report);
    }

    /**
     * 신고 상세 조회 (사용자 정보 및 콘텐츠 포함)
     */
    public ReportDetailResponse getReportDetail(Long reportId) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ReportNotFoundException(reportId));

        // 신고자 정보 조회
        ReportDetailResponse.UserInfo reporter = getUserInfo(report.getReporterId());

        // 신고된 사용자 정보 조회
        ReportDetailResponse.UserInfo reportedUser = report.getReportedUserId() != null
                ? getUserInfo(report.getReportedUserId())
                : null;

        // 처리자 정보 조회
        ReportDetailResponse.UserInfo processedBy = report.getReviewerId() != null
                ? getUserInfo(report.getReviewerId())
                : null;

        // 신고된 콘텐츠 정보 조회
        ReportDetailResponse.TargetContentInfo targetContent = getTargetContent(
                report.getTargetType(),
                report.getTargetId()
        );

        return ReportDetailResponse.from(report, reporter, reportedUser, processedBy, targetContent);
    }

    /**
     * 상태별 신고 목록 조회
     */
    public Page<ReportResponse> getReportsByStatus(ReportStatus status, Pageable pageable) {
        return reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
            .map(this::toReportResponse);
    }

    /**
     * 대기 중인 신고 목록 조회
     */
    public Page<ReportResponse> getPendingReports(Pageable pageable) {
        return reportRepository.findPendingReports(pageable)
            .map(this::toReportResponse);
    }

    /**
     * 고위험 신고 목록 조회
     */
    public Page<ReportResponse> getHighSeverityReports(Pageable pageable) {
        return reportRepository.findHighSeverityReports(pageable)
            .map(this::toReportResponse);
    }

    /**
     * 내가 신고한 목록 조회
     */
    public Page<ReportResponse> getMyReports(Long reporterId, Pageable pageable) {
        return reportRepository.findByReporterIdOrderByCreatedAtDesc(reporterId, pageable)
            .map(this::toReportResponse);
    }

    /**
     * 특정 사용자에 대한 신고 목록 조회
     */
    public Page<ReportResponse> getReportsByUser(Long reportedUserId, Pageable pageable) {
        return reportRepository.findByReportedUserIdOrderByCreatedAtDesc(reportedUserId, pageable)
            .map(this::toReportResponse);
    }

    /**
     * 대상 유형별 신고 목록 조회
     */
    public Page<ReportResponse> getReportsByTargetType(ReportTargetType targetType, Pageable pageable) {
        return reportRepository.findByTargetTypeOrderByCreatedAtDesc(targetType, pageable)
            .map(this::toReportResponse);
    }

    /**
     * 특정 대상에 대한 신고 목록 조회
     */
    public List<ReportResponse> getReportsByTarget(ReportTargetType targetType, Long targetId) {
        return reportRepository.findByTarget(targetType, targetId).stream()
            .map(this::toReportResponse)
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

        // 도메인 이벤트 발행 (ReportReviewStartedEvent)
        eventPublisher.publishEvents(savedReport);

        log.info("신고 검토 시작 완료. reportId: {}", reportId);

        return toReportResponse(savedReport);
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

        // 도메인 이벤트 발행 (ReportApprovedEvent)
        eventPublisher.publishEvents(savedReport);

        log.info("신고 승인 완료. reportId: {}, actionTaken: {}", reportId, request.getActionTaken());

        return toReportResponse(savedReport);
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

        // 도메인 이벤트 발행 (ReportRejectedEvent)
        eventPublisher.publishEvents(savedReport);

        log.info("신고 반려 완료. reportId: {}", reportId);

        return toReportResponse(savedReport);
    }

    /**
     * 신고 처리 (승인/거부) - 프론트엔드 호환용
     */
    @Transactional
    public ReportDetailResponse processReport(Long reportId, Long reviewerId, ProcessReportRequest request) {
        log.info("신고 처리. reportId: {}, reviewerId: {}, status: {}", reportId, reviewerId, request.getStatus());

        // 검토자 존재 확인
        if (!userRepository.existsById(reviewerId)) {
            throw new UserNotFoundException(reviewerId);
        }

        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ReportNotFoundException(reportId));

        if ("APPROVED".equals(request.getStatus())) {
            // 프론트엔드에서 전달한 actionTaken 사용, 없으면 기본값
            String actionTaken = request.getActionTaken() != null
                    ? request.getActionTaken()
                    : (request.getApplyActions() != null && request.getApplyActions()
                        ? "조치 적용됨" : "조치 없음");
            report.approve(reviewerId, request.getProcessingNote(), actionTaken);
        } else if ("REJECTED".equals(request.getStatus())) {
            report.reject(reviewerId, request.getProcessingNote());
        } else {
            throw new IllegalArgumentException("유효하지 않은 상태: " + request.getStatus());
        }

        Report savedReport = reportRepository.save(report);

        // 도메인 이벤트 발행 (ReportApprovedEvent 등)
        eventPublisher.publishEvents(savedReport);

        log.info("신고 처리 완료. reportId: {}, status: {}", reportId, request.getStatus());

        return getReportDetail(savedReport.getId());
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

    /**
     * Report를 ReportResponse로 변환
     */
    private ReportResponse toReportResponse(Report report) {
        ReportResponse.UserInfo reporter = toUserInfo(report.getReporterId());
        ReportResponse.UserInfo reportedUser = report.getReportedUserId() != null
                ? toUserInfo(report.getReportedUserId())
                : null;
        ReportResponse.UserInfo processedBy = report.getReviewerId() != null
                ? toUserInfo(report.getReviewerId())
                : null;

        return ReportResponse.from(report, reporter, reportedUser, processedBy);
    }

    /**
     * 사용자 정보 조회 (ReportResponse용)
     */
    private ReportResponse.UserInfo toUserInfo(Long userId) {
        if (userId == null) {
            return null;
        }

        User user = userRepository.findById(userId)
                .orElse(null);
        if (user == null) {
            return null;
        }

        return ReportResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }

    /**
     * 사용자 정보 조회 (ReportDetailResponse용)
     */
    private ReportDetailResponse.UserInfo getUserInfo(Long userId) {
        if (userId == null) {
            return null;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return ReportDetailResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }

    /**
     * 신고 대상 유효성 검증 (공지사항 및 관리자 콘텐츠 신고 방지)
     */
    private void validateReportTarget(ReportTargetType targetType, Long targetId, Long authorId) {
        switch (targetType) {
            case POST -> {
                Post post = postRepository.findById(targetId)
                        .orElseThrow(() -> new PostNotFoundException(targetId));

                // 공지사항은 신고할 수 없음
                if (post.getIsNoticePost()) {
                    throw new IllegalArgumentException("공지사항은 신고할 수 없습니다.");
                }

                // 관리자가 작성한 게시글은 신고할 수 없음
                if (isAdmin(authorId)) {
                    throw new IllegalArgumentException("관리자가 작성한 게시글은 신고할 수 없습니다.");
                }
            }
            case COMMENT -> {
                // 관리자가 작성한 댓글은 신고할 수 없음
                if (isAdmin(authorId)) {
                    throw new IllegalArgumentException("관리자가 작성한 댓글은 신고할 수 없습니다.");
                }
            }
            case USER -> {
                // 관리자는 신고할 수 없음
                if (isAdmin(authorId)) {
                    throw new IllegalArgumentException("관리자는 신고할 수 없습니다.");
                }
            }
            case CHAT -> {
                // TODO: 채팅 기능 구현 후 추가
            }
        }
    }

    /**
     * 사용자가 관리자인지 확인
     */
    private boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // UserRole이 ADMIN 또는 SUPER_ADMIN인 경우
        return user.getRole().name().contains("ADMIN");
    }

    /**
     * 신고된 콘텐츠 정보 조회
     */
    private ReportDetailResponse.TargetContentInfo getTargetContent(ReportTargetType targetType, Long targetId) {
        return switch (targetType) {
            case POST -> {
                Post post = postRepository.findById(targetId)
                        .orElse(null);
                if (post == null) {
                    yield null;
                }

                ReportDetailResponse.UserInfo author = getUserInfo(post.getAuthorId());
                yield ReportDetailResponse.TargetContentInfo.builder()
                        .id(post.getId())
                        .content(post.getContent())
                        .author(author)
                        .build();
            }
            case COMMENT -> {
                Comment comment = commentRepository.findById(targetId)
                        .orElse(null);
                if (comment == null) {
                    yield null;
                }

                ReportDetailResponse.UserInfo author = getUserInfo(comment.getAuthorId());
                yield ReportDetailResponse.TargetContentInfo.builder()
                        .id(comment.getId())
                        .content(comment.getContent())
                        .author(author)
                        .build();
            }
            case USER -> null; // 사용자 신고는 콘텐츠가 없음
            case CHAT -> {
                // TODO: 채팅 기능 구현 후 추가
                yield null;
            }
        };
    }
}
