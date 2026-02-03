package com.solif.backend.domain.councilreview.dto.response;

import com.solif.backend.domain.councilreview.entity.CouncilReviewRelay;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "자치회 활동 후기 릴레이 응답")
public class CouncilReviewRelayResponse {

    @Schema(description = "릴레이 ID", example = "1")
    private Long councilReviewRelayId;

    @Schema(description = "작성자 사용자 ID", example = "1")
    private Long writerUserId;

    @Schema(description = "작성자 이름", example = "김선한")
    private String writerUserName;

    @Schema(description = "릴레이 순서", example = "1")
    private Integer relayOrder;

    @Schema(description = "질문 내용", example = "오늘 먹은 음식은요?")
    private String questionText;

    @Schema(description = "릴레이 내용", example = "등촌 한국수를 먹으며...")
    private String relayContent;

    @Schema(description = "내가 작성한 릴레이인지 여부", example = "true")
    private Boolean isMyRelay;

    @Schema(description = "작성 시간", example = "2026-02-10T15:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "null")
    private LocalDateTime updatedAt;

    public static CouncilReviewRelayResponse from(
            CouncilReviewRelay relay,
            Long currentUserId
    ) {
        return CouncilReviewRelayResponse.builder()
                .councilReviewRelayId(relay.getCouncilReviewRelayId())
                .writerUserId(relay.getWriterUser().getUserId())
                .writerUserName(relay.getWriterUser().getName())
                .relayOrder(relay.getRelayOrder())
                .questionText(relay.getCouncilReviewQuestion().getQuestionText())
                .relayContent(relay.getRelayContent())
                .isMyRelay(relay.isWriter(currentUserId))
                .createdAt(relay.getCreatedAt())
                .updatedAt(relay.getUpdatedAt())
                .build();
    }
}