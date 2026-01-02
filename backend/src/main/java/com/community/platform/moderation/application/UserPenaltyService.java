package com.community.platform.moderation.application;

import com.community.platform.moderation.domain.PenaltyType;
import com.community.platform.moderation.domain.UserPenalty;
import com.community.platform.moderation.exception.PenaltyNotFoundException;
import com.community.platform.moderation.infrastructure.persistence.UserPenaltyRepository;
import com.community.platform.shared.application.DomainEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * 사용자 제재 서비스
 * 제재 부여, 조회, 만료 등의 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPenaltyService {

    private final UserPenaltyRepository penaltyRepository;
    private final DomainEventService domainEventService;

    /**
     * 제재 부여
     */
    @Transactional
    public UserPenalty applyPenalty(Long userId, PenaltyType type, String reason, Long adminId) {
        log.info("제재 부여. userId: {}, type: {}, reason: {}", userId, type, reason);

        UserPenalty penalty = UserPenalty.create(userId, type, reason, adminId);
        penalty = penaltyRepository.save(penalty);

        // 도메인 이벤트 발행
        domainEventService.publishEvents(penalty);

        return penalty;
    }

    /**
     * 제재 부여 (기간 지정)
     */
    @Transactional
    public UserPenalty applyPenalty(Long userId, PenaltyType type, String reason,
                                    Duration duration, Long adminId) {
        log.info("제재 부여 (기간 지정). userId: {}, type: {}, duration: {}", userId, type, duration);

        UserPenalty penalty = UserPenalty.create(userId, type, reason, duration, adminId);
        penalty = penaltyRepository.save(penalty);

        // 도메인 이벤트 발행
        domainEventService.publishEvents(penalty);

        return penalty;
    }

    /**
     * 특정 타입의 활성 제재 여부 확인
     */
    public boolean hasActivePenalty(Long userId, PenaltyType type) {
        List<UserPenalty> penalties = penaltyRepository.findActiveByUserIdAndTypes(
                userId, List.of(type)
        );
        return !penalties.isEmpty();
    }

    /**
     * 글쓰기 금지 제재 여부 확인
     */
    public boolean hasPostBan(Long userId) {
        List<PenaltyType> postBanTypes = Arrays.asList(
                PenaltyType.POST_BAN_24H,
                PenaltyType.POST_BAN_7D,
                PenaltyType.POST_BAN_PERMANENT,
                PenaltyType.FULL_BAN
        );
        List<UserPenalty> penalties = penaltyRepository.findActiveByUserIdAndTypes(userId, postBanTypes);
        return !penalties.isEmpty();
    }

    /**
     * 댓글 금지 제재 여부 확인
     */
    public boolean hasCommentBan(Long userId) {
        List<PenaltyType> commentBanTypes = Arrays.asList(
                PenaltyType.COMMENT_BAN_24H,
                PenaltyType.COMMENT_BAN_7D,
                PenaltyType.COMMENT_BAN_PERMANENT,
                PenaltyType.FULL_BAN
        );
        List<UserPenalty> penalties = penaltyRepository.findActiveByUserIdAndTypes(userId, commentBanTypes);
        return !penalties.isEmpty();
    }

    /**
     * 사용자의 활성 제재 목록 조회
     */
    public List<UserPenalty> getActivePenalties(Long userId) {
        return penaltyRepository.findActiveByUserId(userId);
    }

    /**
     * 사용자의 모든 제재 이력 조회
     */
    public Page<UserPenalty> getPenaltyHistory(Long userId, Pageable pageable) {
        return penaltyRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 제재 ID로 조회
     */
    public UserPenalty getPenaltyById(Long penaltyId) {
        return penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new PenaltyNotFoundException(penaltyId));
    }

    /**
     * 제재 해제 (수동 만료)
     */
    @Transactional
    public void expirePenalty(Long penaltyId) {
        log.info("제재 수동 해제. penaltyId: {}", penaltyId);

        UserPenalty penalty = getPenaltyById(penaltyId);
        penalty.expire();

        // 도메인 이벤트 발행
        domainEventService.publishEvents(penalty);
    }

    /**
     * 만료된 제재 자동 처리
     * 스케줄러에서 주기적으로 호출
     */
    @Transactional
    public void checkAndAutoExpire() {
        List<UserPenalty> expiredPenalties = penaltyRepository.findExpiredPenalties();

        if (expiredPenalties.isEmpty()) {
            return;
        }

        log.info("만료된 제재 자동 처리. 개수: {}", expiredPenalties.size());

        for (UserPenalty penalty : expiredPenalties) {
            penalty.expire();
            domainEventService.publishEvents(penalty);
        }
    }

    /**
     * 활성화된 모든 제재 목록 조회 (관리자용)
     */
    public Page<UserPenalty> getActivePenaltiesForAdmin(Pageable pageable) {
        return penaltyRepository.findActivePenalties(pageable);
    }

    /**
     * 사용자가 글쓰기 가능한지 확인
     */
    public boolean canCreatePost(Long userId) {
        return !hasPostBan(userId);
    }

    /**
     * 사용자가 댓글 작성 가능한지 확인
     */
    public boolean canCreateComment(Long userId) {
        return !hasCommentBan(userId);
    }
}
