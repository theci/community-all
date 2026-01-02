package com.community.platform.notification.infrastructure.persistence;

import com.community.platform.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 알림 리포지토리
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 사용자의 알림 목록 조회 (최신순)
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * 사용자의 읽지 않은 알림 개수 조회
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    Long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 모든 알림 읽음 처리
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt " +
           "WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    /**
     * 특정 기간보다 오래된 읽은 알림 삭제 (정리 작업용)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.createdAt < :beforeDate")
    void deleteReadNotificationsOlderThan(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * 사용자의 특정 알림 삭제
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.id = :notificationId AND n.userId = :userId")
    void deleteByIdAndUserId(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    /**
     * 사용자의 읽지 않은 알림 조회 (최신순)
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    Page<Notification> findUnreadByUserId(@Param("userId") Long userId, Pageable pageable);
}
