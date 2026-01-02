package com.community.platform.notification.infrastructure.persistence;

import com.community.platform.notification.domain.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 알림 설정 리포지토리
 */
@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    /**
     * 사용자의 알림 설정 조회
     */
    Optional<NotificationPreference> findByUserId(Long userId);

    /**
     * 사용자의 알림 설정 존재 여부 확인
     */
    boolean existsByUserId(Long userId);
}
