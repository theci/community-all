package com.community.platform.messaging.infrastructure.persistence;

import com.community.platform.messaging.domain.MessageThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageThreadRepository extends JpaRepository<MessageThread, Long> {

    // 두 사용자 간의 스레드 조회 (정렬된 ID로 조회)
    @Query("SELECT t FROM MessageThread t WHERE " +
           "(t.user1Id = :smallerId AND t.user2Id = :largerId) OR " +
           "(t.user1Id = :largerId AND t.user2Id = :smallerId)")
    Optional<MessageThread> findByUsers(@Param("smallerId") Long smallerId,
                                        @Param("largerId") Long largerId);

    // 특정 사용자의 모든 대화 스레드 조회 (최근 메시지 순)
    @Query("SELECT t FROM MessageThread t WHERE t.user1Id = :userId OR t.user2Id = :userId ORDER BY t.lastMessageAt DESC")
    Page<MessageThread> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // 특정 사용자의 대화 스레드 개수
    @Query("SELECT COUNT(t) FROM MessageThread t WHERE t.user1Id = :userId OR t.user2Id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}
