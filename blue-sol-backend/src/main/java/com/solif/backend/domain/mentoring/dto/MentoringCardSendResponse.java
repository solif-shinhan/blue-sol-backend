package com.solif.backend.domain.mentoring.dto;

import com.solif.backend.domain.mentoring.entity.MentoringCard;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "멘토링 엽서 발송 응답")
public class MentoringCardSendResponse {

    @Schema(description = "멘토링 엽서 ID", example = "1")
    private Long mentoringCardId;

    @Schema(description = "발송자 이름", example = "김철수")
    private String senderName;

    @Schema(description = "발송 일시", example = "2026-02-09T12:00:00")
    private LocalDateTime createdAt;

    public static MentoringCardSendResponse from(MentoringCard card) {
        return MentoringCardSendResponse.builder()
                .mentoringCardId(card.getMentoringCardId())
                .senderName(card.getSender().getName())
                .createdAt(card.getCreatedAt())
                .build();
    }
}