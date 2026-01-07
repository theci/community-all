package com.community.platform.messaging.application;

import com.community.platform.messaging.domain.Message;
import com.community.platform.messaging.domain.MessageThread;
import com.community.platform.messaging.dto.MessageResponse;
import com.community.platform.messaging.dto.MessageThreadResponse;
import com.community.platform.user.domain.User;
import com.community.platform.user.dto.UserSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Message 도메인 객체와 DTO 간 매핑을 담당하는 MapStruct 매퍼
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MessageMapper {

    /**
     * User를 UserSummaryResponse로 변환
     */
    @Mapping(source = "nickname", target = "nickname")
    @Mapping(target = "profileImageUrl", ignore = true)
    UserSummaryResponse toUserSummaryResponse(User user);

    /**
     * Message 엔티티를 MessageResponse DTO로 변환 (상세 정보 포함)
     */
    default MessageResponse toMessageResponse(Message message, User sender, User recipient, Long currentUserId) {
        return MessageResponse.builder()
                .id(message.getId())
                .threadId(message.getThread() != null ? message.getThread().getId() : null)
                .sender(toUserSummaryResponse(sender))
                .recipient(toUserSummaryResponse(recipient))
                .content(message.getContent())
                .status(message.getStatus())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .isSender(message.getSenderId().equals(currentUserId))
                .build();
    }

    /**
     * MessageThread를 MessageThreadResponse로 변환
     */
    default MessageThreadResponse toThreadResponse(MessageThread thread, User otherUser,
                                                   MessageResponse lastMessage, Integer unreadCount) {
        return MessageThreadResponse.builder()
                .id(thread.getId())
                .otherUser(toUserSummaryResponse(otherUser))
                .lastMessage(lastMessage)
                .unreadCount(unreadCount)
                .lastMessageAt(thread.getLastMessageAt())
                .createdAt(thread.getCreatedAt())
                .build();
    }
}
