package com.community.platform.messaging.domain;

import com.community.platform.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "message_threads",
       indexes = {
           @Index(name = "idx_thread_user1_user2", columnList = "user1_id, user2_id", unique = true),
           @Index(name = "idx_thread_last_message", columnList = "last_message_at")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageThread extends BaseEntity {

    @Column(name = "user1_id", nullable = false)
    private Long user1Id;

    @Column(name = "user2_id", nullable = false)
    private Long user2Id;

    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    private MessageThread(Long user1Id, Long user2Id) {
        // user1Id < user2Id 보장 (정렬하여 중복 방지)
        if (user1Id.compareTo(user2Id) < 0) {
            this.user1Id = user1Id;
            this.user2Id = user2Id;
        } else {
            this.user1Id = user2Id;
            this.user2Id = user1Id;
        }
        this.lastMessageAt = LocalDateTime.now();
    }

    public static MessageThread create(Long user1Id, Long user2Id) {
        if (user1Id == null || user2Id == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (user1Id.equals(user2Id)) {
            throw new IllegalArgumentException("같은 사용자 간에는 대화를 생성할 수 없습니다.");
        }
        return new MessageThread(user1Id, user2Id);
    }

    public void updateLastMessageAt() {
        this.lastMessageAt = LocalDateTime.now();
    }

    public boolean involvesUser(Long userId) {
        return this.user1Id.equals(userId) || this.user2Id.equals(userId);
    }

    public Long getOtherUserId(Long userId) {
        if (!involvesUser(userId)) {
            throw new IllegalArgumentException("해당 사용자는 이 대화에 속해있지 않습니다.");
        }
        return this.user1Id.equals(userId) ? this.user2Id : this.user1Id;
    }
}
