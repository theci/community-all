package com.community.platform.messaging.application;

import com.community.platform.messaging.domain.Message;
import com.community.platform.messaging.domain.MessageStatus;
import com.community.platform.messaging.domain.MessageThread;
import com.community.platform.messaging.dto.MessageResponse;
import com.community.platform.messaging.dto.MessageThreadResponse;
import com.community.platform.messaging.exception.MessageNotFoundException;
import com.community.platform.messaging.exception.MessageThreadNotFoundException;
import com.community.platform.messaging.exception.UnauthorizedMessageAccessException;
import com.community.platform.messaging.infrastructure.persistence.MessageRepository;
import com.community.platform.messaging.infrastructure.persistence.MessageThreadRepository;
import com.community.platform.shared.infrastructure.DomainEventPublisher;
import com.community.platform.user.domain.User;
import com.community.platform.user.exception.UserNotFoundException;
import com.community.platform.user.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 메시지 관리 애플리케이션 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageThreadRepository threadRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final DomainEventPublisher eventPublisher;

    /**
     * 메시지 전송
     */
    @Transactional
    public MessageResponse sendMessage(Long senderId, Long recipientId, String content) {
        log.info("메시지 전송 시작. senderId: {}, recipientId: {}", senderId, recipientId);

        // 발신자와 수신자 검증
        User sender = getUserById(senderId);
        User recipient = getUserById(recipientId);

        // 두 사용자 간의 스레드 찾기 또는 생성
        MessageThread thread = getOrCreateThread(senderId, recipientId);

        // 메시지 생성
        Message message = Message.create(thread, senderId, recipientId, content);
        Message savedMessage = messageRepository.save(message);

        // 도메인 이벤트 발행
        eventPublisher.publishEvents(savedMessage);

        log.info("메시지 전송 완료. messageId: {}", savedMessage.getId());
        return messageMapper.toMessageResponse(savedMessage, sender, recipient, senderId);
    }

    /**
     * 메시지 조회
     */
    public MessageResponse getMessage(Long messageId, Long currentUserId) {
        log.info("메시지 조회. messageId: {}, currentUserId: {}", messageId, currentUserId);

        Message message = getMessageById(messageId);

        // 권한 확인 (발신자 또는 수신자만 조회 가능)
        if (!message.getSenderId().equals(currentUserId) &&
            !message.getRecipientId().equals(currentUserId)) {
            throw new UnauthorizedMessageAccessException();
        }

        User sender = getUserById(message.getSenderId());
        User recipient = getUserById(message.getRecipientId());

        return messageMapper.toMessageResponse(message, sender, recipient, currentUserId);
    }

    /**
     * 특정 스레드의 메시지 목록 조회
     */
    public Page<MessageResponse> getMessagesByThread(Long threadId, Long currentUserId, Pageable pageable) {
        log.info("스레드 메시지 목록 조회. threadId: {}, currentUserId: {}", threadId, currentUserId);

        MessageThread thread = getThreadById(threadId);

        // 권한 확인 (스레드 참여자만 조회 가능)
        if (!thread.involvesUser(currentUserId)) {
            throw new UnauthorizedMessageAccessException();
        }

        Page<Message> messages = messageRepository.findByThreadIdAndStatus(
            threadId, MessageStatus.ACTIVE, pageable);

        return messages.map(message -> {
            User sender = getUserById(message.getSenderId());
            User recipient = getUserById(message.getRecipientId());
            return messageMapper.toMessageResponse(message, sender, recipient, currentUserId);
        });
    }

    /**
     * 받은 메시지 목록 조회
     */
    public Page<MessageResponse> getReceivedMessages(Long userId, Pageable pageable) {
        log.info("받은 메시지 목록 조회. userId: {}", userId);

        Page<Message> messages = messageRepository.findByRecipientIdAndStatus(
            userId, MessageStatus.ACTIVE, pageable);

        return messages.map(message -> {
            User sender = getUserById(message.getSenderId());
            User recipient = getUserById(message.getRecipientId());
            return messageMapper.toMessageResponse(message, sender, recipient, userId);
        });
    }

    /**
     * 보낸 메시지 목록 조회
     */
    public Page<MessageResponse> getSentMessages(Long userId, Pageable pageable) {
        log.info("보낸 메시지 목록 조회. userId: {}", userId);

        Page<Message> messages = messageRepository.findBySenderIdAndStatus(
            userId, MessageStatus.ACTIVE, pageable);

        return messages.map(message -> {
            User sender = getUserById(message.getSenderId());
            User recipient = getUserById(message.getRecipientId());
            return messageMapper.toMessageResponse(message, sender, recipient, userId);
        });
    }

    /**
     * 대화 스레드 목록 조회
     */
    public Page<MessageThreadResponse> getThreads(Long userId, Pageable pageable) {
        log.info("대화 스레드 목록 조회. userId: {}", userId);

        Page<MessageThread> threads = threadRepository.findByUserId(userId, pageable);

        return threads.map(thread -> {
            Long otherUserId = thread.getOtherUserId(userId);
            User otherUser = getUserById(otherUserId);

            Message lastMessage = messageRepository.findLastMessageByThreadId(
                thread.getId(), MessageStatus.ACTIVE);

            Integer unreadCount = messageRepository.countUnreadMessagesByThread(
                thread.getId(), userId, MessageStatus.ACTIVE);

            MessageResponse lastMessageResponse = null;
            if (lastMessage != null) {
                User sender = getUserById(lastMessage.getSenderId());
                User recipient = getUserById(lastMessage.getRecipientId());
                lastMessageResponse = messageMapper.toMessageResponse(
                    lastMessage, sender, recipient, userId);
            }

            return messageMapper.toThreadResponse(
                thread, otherUser, lastMessageResponse, unreadCount);
        });
    }

    /**
     * 메시지 읽음 처리
     */
    @Transactional
    public void markAsRead(Long messageId, Long currentUserId) {
        log.info("메시지 읽음 처리. messageId: {}, currentUserId: {}", messageId, currentUserId);

        Message message = getMessageById(messageId);

        // 권한 확인 (수신자만 읽음 처리 가능)
        if (!message.canBeReadBy(currentUserId)) {
            throw new UnauthorizedMessageAccessException();
        }

        message.markAsRead();
        log.info("메시지 읽음 처리 완료. messageId: {}", messageId);
    }

    /**
     * 특정 스레드의 모든 메시지 읽음 처리
     */
    @Transactional
    public void markThreadAsRead(Long threadId, Long currentUserId) {
        log.info("스레드 전체 읽음 처리. threadId: {}, currentUserId: {}", threadId, currentUserId);

        MessageThread thread = getThreadById(threadId);

        // 권한 확인
        if (!thread.involvesUser(currentUserId)) {
            throw new UnauthorizedMessageAccessException();
        }

        List<Message> unreadMessages = messageRepository.findByThreadIdAndStatusOrderByCreatedAt(
            threadId, MessageStatus.ACTIVE).stream()
            .filter(m -> m.getRecipientId().equals(currentUserId) && !m.getIsRead())
            .toList();

        unreadMessages.forEach(Message::markAsRead);
        log.info("스레드 전체 읽음 처리 완료. threadId: {}, 처리된 메시지 수: {}",
                 threadId, unreadMessages.size());
    }

    /**
     * 메시지 삭제
     */
    @Transactional
    public void deleteMessage(Long messageId, Long currentUserId) {
        log.info("메시지 삭제. messageId: {}, currentUserId: {}", messageId, currentUserId);

        Message message = getMessageById(messageId);

        // 권한 확인 (발신자 또는 수신자만 삭제 가능)
        if (!message.canBeDeletedBy(currentUserId)) {
            throw new UnauthorizedMessageAccessException();
        }

        message.delete();
        log.info("메시지 삭제 완료. messageId: {}", messageId);
    }

    /**
     * 안 읽은 메시지 수 조회
     */
    public Long getUnreadCount(Long userId) {
        return messageRepository.countUnreadMessages(userId, MessageStatus.ACTIVE);
    }

    // === 헬퍼 메서드 ===

    private Message getMessageById(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));
    }

    private MessageThread getThreadById(Long threadId) {
        return threadRepository.findById(threadId)
                .orElseThrow(() -> new MessageThreadNotFoundException(threadId));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private MessageThread getOrCreateThread(Long user1Id, Long user2Id) {
        Long smallerId = user1Id.compareTo(user2Id) < 0 ? user1Id : user2Id;
        Long largerId = user1Id.compareTo(user2Id) < 0 ? user2Id : user1Id;

        return threadRepository.findByUsers(smallerId, largerId)
                .orElseGet(() -> {
                    MessageThread newThread = MessageThread.create(user1Id, user2Id);
                    return threadRepository.save(newThread);
                });
    }
}
