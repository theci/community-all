package com.community.platform.messaging.infrastructure.persistence;

import com.community.platform.messaging.domain.Message;
import com.community.platform.messaging.domain.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // 특정 스레드의 메시지 목록 조회 (시간순)
    @Query("SELECT m FROM Message m WHERE m.thread.id = :threadId AND m.status = :status ORDER BY m.createdAt ASC")
    List<Message> findByThreadIdAndStatusOrderByCreatedAt(@Param("threadId") Long threadId,
                                                          @Param("status") MessageStatus status);

    // 특정 스레드의 메시지 목록 페이징 조회
    @Query("SELECT m FROM Message m WHERE m.thread.id = :threadId AND m.status = :status ORDER BY m.createdAt DESC")
    Page<Message> findByThreadIdAndStatus(@Param("threadId") Long threadId,
                                          @Param("status") MessageStatus status,
                                          Pageable pageable);

    // 특정 사용자가 받은 메시지 조회 (받은 쪽지함)
    @Query("SELECT m FROM Message m WHERE m.recipientId = :userId AND m.status = :status ORDER BY m.createdAt DESC")
    Page<Message> findByRecipientIdAndStatus(@Param("userId") Long userId,
                                             @Param("status") MessageStatus status,
                                             Pageable pageable);

    // 특정 사용자가 보낸 메시지 조회 (보낸 쪽지함)
    @Query("SELECT m FROM Message m WHERE m.senderId = :userId AND m.status = :status ORDER BY m.createdAt DESC")
    Page<Message> findBySenderIdAndStatus(@Param("userId") Long userId,
                                          @Param("status") MessageStatus status,
                                          Pageable pageable);

    // 특정 사용자의 안 읽은 메시지 수
    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipientId = :userId AND m.isRead = false AND m.status = :status")
    Long countUnreadMessages(@Param("userId") Long userId, @Param("status") MessageStatus status);

    // 특정 스레드의 안 읽은 메시지 수 (특정 사용자 기준)
    @Query("SELECT COUNT(m) FROM Message m WHERE m.thread.id = :threadId AND m.recipientId = :userId AND m.isRead = false AND m.status = :status")
    Integer countUnreadMessagesByThread(@Param("threadId") Long threadId,
                                        @Param("userId") Long userId,
                                        @Param("status") MessageStatus status);

    // 특정 스레드의 마지막 메시지 조회
    @Query("SELECT m FROM Message m WHERE m.thread.id = :threadId AND m.status = :status ORDER BY m.createdAt DESC LIMIT 1")
    Message findLastMessageByThreadId(@Param("threadId") Long threadId,
                                      @Param("status") MessageStatus status);

    // 특정 사용자와 관련된 모든 메시지 조회 (보낸 것 + 받은 것)
    @Query("SELECT m FROM Message m WHERE (m.senderId = :userId OR m.recipientId = :userId) AND m.status = :status ORDER BY m.createdAt DESC")
    Page<Message> findByUserIdAndStatus(@Param("userId") Long userId,
                                        @Param("status") MessageStatus status,
                                        Pageable pageable);
}
