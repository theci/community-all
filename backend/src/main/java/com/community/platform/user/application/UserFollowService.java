package com.community.platform.user.application;

import com.community.platform.user.domain.UserFollow;
import com.community.platform.user.exception.UserNotFoundException;
import com.community.platform.user.infrastructure.persistence.UserFollowRepository;
import com.community.platform.user.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자 팔로우 비즈니스 로직 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFollowService {

    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;

    /**
     * 팔로우
     */
    @Transactional
    public void follow(Long followerId, Long followingId) {
        log.info("팔로우 요청: followerId={}, followingId={}", followerId, followingId);

        // 사용자 존재 확인
        validateUserExists(followerId);
        validateUserExists(followingId);

        // 이미 팔로우 중인지 확인
        if (userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            log.warn("이미 팔로우 중입니다: followerId={}, followingId={}", followerId, followingId);
            return; // 중복 팔로우 방지
        }

        // 팔로우 생성
        UserFollow userFollow = UserFollow.create(followerId, followingId);
        userFollowRepository.save(userFollow);

        log.info("팔로우 완료: followerId={}, followingId={}", followerId, followingId);
    }

    /**
     * 언팔로우
     */
    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        log.info("언팔로우 요청: followerId={}, followingId={}", followerId, followingId);

        // 팔로우 관계 확인 및 삭제
        if (!userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            log.warn("팔로우 관계가 존재하지 않습니다: followerId={}, followingId={}", followerId, followingId);
            return;
        }

        userFollowRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
        log.info("언팔로우 완료: followerId={}, followingId={}", followerId, followingId);
    }

    /**
     * 팔로우 상태 확인
     */
    public boolean isFollowing(Long followerId, Long followingId) {
        return userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    /**
     * 팔로잉 목록 조회 (내가 팔로우하는 사용자 ID 목록)
     */
    public List<Long> getFollowingIds(Long userId) {
        return userFollowRepository.findFollowingIdsByFollowerId(userId);
    }

    /**
     * 팔로워 목록 조회 (나를 팔로우하는 사용자 ID 목록)
     */
    public List<Long> getFollowerIds(Long userId) {
        return userFollowRepository.findFollowerIdsByFollowingId(userId);
    }

    /**
     * 팔로잉 수 조회
     */
    public long getFollowingCount(Long userId) {
        return userFollowRepository.countByFollowerId(userId);
    }

    /**
     * 팔로워 수 조회
     */
    public long getFollowerCount(Long userId) {
        return userFollowRepository.countByFollowingId(userId);
    }

    /**
     * 사용자 존재 확인
     */
    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId);
        }
    }
}
