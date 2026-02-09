package com.solif.backend.domain.mentoring.dto;

import com.solif.backend.domain.mentoring.entity.MentoringCard;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "멘토링 엽서 상세 응답")
public class MentoringCardDetailResponse {

    @Schema(description = "멘토링 엽서 ID", example = "1")
    private Long mentoringCardId;

    @Schema(description = "발송자 ID", example = "1")
    private Long senderId;

    @Schema(description = "발송자 이름", example = "김철수")
    private String senderName;

    @Schema(description = "엽서 제목", example = "금융권 IB 직무 멘토를 찾고 싶습니다")
    private String cardTitle;

    @Schema(description = "카테고리", example = "JOB")
    private String category;

    @Schema(description = "멘토링 방식", example = "VIDEO")
    private String method;

    @Schema(description = "엽서 내용", example = "안녕하세요. 저는 한국대학교에 재학중인 학생입니다...")
    private String cardContent;

    @Schema(description = "읽음 여부", example = "false")
    private Boolean isRead;

    @Schema(description = "읽은 시간", nullable = true)
    private LocalDateTime readAt;

    @Schema(description = "발송 일시", example = "2026-02-09T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "첨부 파일 URL 목록", nullable = true)
    private List<String> fileUrls;

    public static MentoringCardDetailResponse from(MentoringCard card, List<String> fileUrls) {
        return MentoringCardDetailResponse.builder()
                .mentoringCardId(card.getMentoringCardId())
                .senderId(card.getSender().getUserId())
                .senderName(card.getSender().getName())
                .cardTitle(card.getCardTitle())
                .category(card.getCategory().name())
                .method(card.getMethod().name())
                .cardContent(card.getCardContent())
                .isRead(card.getIsRead())
                .readAt(card.getReadAt())
                .createdAt(card.getCreatedAt())
                .fileUrls(fileUrls)
                .build();
    }
}