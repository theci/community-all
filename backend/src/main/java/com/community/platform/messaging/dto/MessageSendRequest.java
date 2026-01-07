package com.community.platform.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 메시지 전송 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class MessageSendRequest {

    @NotNull(message = "수신자 ID는 필수입니다")
    private Long recipientId; // 수신자 ID

    @NotBlank(message = "메시지 내용은 필수입니다")
    @Size(max = 5000, message = "메시지는 5000자를 초과할 수 없습니다")
    private String content; // 메시지 내용
}
