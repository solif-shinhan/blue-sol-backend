package com.solif.backend.domain.message.dto;

import com.solif.backend.domain.message.entity.Message;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "쪽지 목록 응답")
public class MessageListResponse {

    @Schema(description = "쪽지 ID", example = "1")
    private Long messageId;

    @Schema(description = "상대방 이름 (받은 쪽지: 보낸 사람, 보낸 쪽지: 받는 사람)", example = "박민수")
    private String counterpartName;

    @Schema(description = "쪽지 제목", example = "한국대학교 재학생 박민수 입니다.")
    private String messageTitle;

    @Schema(description = "읽음 여부", example = "false")
    private Boolean isRead;

    @Schema(description = "발송 일시", example = "2026-01-31T12:00:00")
    private LocalDateTime createdAt;

    // 받은 쪽지용
    public static MessageListResponse fromReceived(Message message) {
        return MessageListResponse.builder()
                .messageId(message.getMessageId())
                .counterpartName(message.getSender().getName())
                .messageTitle(message.getMessageTitle())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .build();
    }

    // 보낸 쪽지용
    public static MessageListResponse fromSent(Message message) {
        return MessageListResponse.builder()
                .messageId(message.getMessageId())
                .counterpartName(message.getReceiver().getName())
                .messageTitle(message.getMessageTitle())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
