package com.solif.backend.domain.mentoring.dto;

import com.solif.backend.domain.mentoring.entity.MentoringCard;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "멘토링 엽서 목록 응답")
public class MentoringCardListResponse {

    @Schema(description = "멘토링 엽서 ID", example = "1")
    private Long mentoringCardId;

    @Schema(description = "발송자 이름", example = "김철수")
    private String senderName;

    @Schema(description = "엽서 제목", example = "금융권 IB 직무 멘토를 찾고 싶습니다")
    private String cardTitle;

    @Schema(description = "카테고리", example = "JOB")
    private String category;

    @Schema(description = "멘토링 방식", example = "VIDEO")
    private String method;

    @Schema(description = "읽음 여부", example = "false")
    private Boolean isRead;

    @Schema(description = "발송 일시", example = "2026-02-09T12:00:00")
    private LocalDateTime createdAt;

    public static MentoringCardListResponse from(MentoringCard card) {
        return MentoringCardListResponse.builder()
                .mentoringCardId(card.getMentoringCardId())
                .senderName(card.getSender().getName())
                .cardTitle(card.getCardTitle())
                .category(card.getCategory().name())
                .method(card.getMethod().name())
                .isRead(card.getIsRead())
                .createdAt(card.getCreatedAt())
                .build();
    }
}