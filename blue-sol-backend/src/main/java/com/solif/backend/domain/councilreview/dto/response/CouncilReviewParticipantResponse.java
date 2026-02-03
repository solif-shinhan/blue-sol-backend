package com.solif.backend.domain.councilreview.dto.response;

import com.solif.backend.domain.councilreview.entity.CouncilReviewParticipant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "자치회 활동 참여자 응답")
public class CouncilReviewParticipantResponse {

    @Schema(description = "참여자 사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "참여자 이름", example = "김선한")
    private String userName;

    public static CouncilReviewParticipantResponse from(CouncilReviewParticipant participant) {
        return CouncilReviewParticipantResponse.builder()
                .userId(participant.getUser().getUserId())
                .userName(participant.getUser().getName())
                .build();
    }
}