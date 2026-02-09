package com.solif.backend.domain.message.dto;

import com.solif.backend.domain.message.entity.Message;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "쪽지 상세 응답")
public class MessageDetailResponse {

    @Schema(description = "쪽지 ID", example = "1")
    private Long messageId;

    @Schema(description = "보낸 사람 ID", example = "2")
    private Long senderId;

    @Schema(description = "보낸 사람 이름", example = "박민수")
    private String senderName;

    @Schema(description = "받는 사람 ID", example = "2")
    private Long receiverId;

    @Schema(description = "받는 사람 이름", example = "박민수")
    private String receiverName;

    @Schema(description = "쪽지 제목", example = "한국대학교 재학생 박민수 입니다.")
    private String messageTitle;

    @Schema(description = "쪽지 내용")
    private String messageContent;

    @Schema(description = "읽음 여부", example = "false")
    private Boolean isRead;

    @Schema(description = "읽은 시간", nullable = true)
    private LocalDateTime readAt;

    @Schema(description = "발송 일시", example = "2026-01-31T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "첨부 이미지 URL 목록")
    private List<String> imageUrls;

    public static MessageDetailResponse from(Message message, List<String> imageUrls) {
        return MessageDetailResponse.builder()
                .messageId(message.getMessageId())
                .senderId(message.getSender().getUserId())
                .senderName(message.getSender().getName())
                .receiverId(message.getReceiver().getUserId())
                .receiverName(message.getReceiver().getName())
                .messageTitle(message.getMessageTitle())
                .messageContent(message.getMessageContent())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .imageUrls(imageUrls)
                .build();
    }
}
