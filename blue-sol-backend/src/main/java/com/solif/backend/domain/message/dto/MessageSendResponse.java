package com.solif.backend.domain.message.dto;

import com.solif.backend.domain.message.entity.Message;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "쪽지 발송 응답")
public class MessageSendResponse {

    @Schema(description = "쪽지 ID", example = "1")
    private Long messageId;

    @Schema(description = "발송 일시", example = "2026-01-31T12:00:00")
    private LocalDateTime createdAt;

    public static MessageSendResponse from(Message message) {
        return MessageSendResponse.builder()
                .messageId(message.getMessageId())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
