package com.community.platform.content.infrastructure.redis;

import com.community.platform.content.infrastructure.persistence.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis 기반 조회수 중복 방지 서비스
 * IP + User ID 조합으로 24시간 동안 중복 조회 방지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ViewCountService {

    private static final String VIEW_KEY_PREFIX = "post:view:";
    private static final int VIEW_TTL_HOURS = 24;

    private final RedisTemplate<String, String> redisTemplate;
    private final PostRepository postRepository;

    /**
     * 조회수 증가 (중복 방지)
     * @param postId 게시글 ID
     * @param userId 사용자 ID (비로그인 시 null)
     * @param ip 클라이언트 IP 주소
     * @return 조회수가 증가되었으면 true, 이미 조회한 경우 false
     */
    public boolean incrementViewCount(Long postId, String userId, String ip) {
        try {
            String key = VIEW_KEY_PREFIX + postId;
            String identifier = buildIdentifier(userId, ip);

            // Redis Set에 추가 시도 (중복이면 0 반환, 추가되면 1 반환)
            Long added = redisTemplate.opsForSet().add(key, identifier);

            if (added != null && added > 0) {
                // 처음 조회한 경우 - DB 조회수 증가
                postRepository.incrementViewCount(postId);

                // TTL 설정 (24시간)
                redisTemplate.expire(key, Duration.ofHours(VIEW_TTL_HOURS));

                log.debug("조회수 증가 완료. postId: {}, identifier: {}", postId, identifier);
                return true;
            } else {
                log.debug("이미 조회한 게시글. postId: {}, identifier: {}", postId, identifier);
                return false;
            }
        } catch (Exception e) {
            // Redis 장애 시 fallback: 매번 조회수 증가
            log.error("Redis 조회수 처리 실패. postId: {}, fallback to direct increment", postId, e);
            postRepository.incrementViewCount(postId);
            return true;
        }
    }

    /**
     * 사용자 식별자 생성
     * 로그인 사용자: user:{userId}
     * 비로그인 사용자: ip:{ipAddress}
     */
    private String buildIdentifier(String userId, String ip) {
        if (userId != null && !userId.isEmpty()) {
            return "user:" + userId;
        }
        return "ip:" + (ip != null ? ip : "unknown");
    }

    /**
     * 특정 게시글의 조회 기록 삭제 (테스트용)
     */
    public void clearViewHistory(Long postId) {
        String key = VIEW_KEY_PREFIX + postId;
        redisTemplate.delete(key);
        log.info("조회 기록 삭제. postId: {}", postId);
    }

    /**
     * 사용자가 게시글을 조회했는지 확인
     */
    public boolean hasViewed(Long postId, String userId, String ip) {
        try {
            String key = VIEW_KEY_PREFIX + postId;
            String identifier = buildIdentifier(userId, ip);
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, identifier));
        } catch (Exception e) {
            log.error("Redis 조회 확인 실패. postId: {}", postId, e);
            return false;
        }
    }
}
