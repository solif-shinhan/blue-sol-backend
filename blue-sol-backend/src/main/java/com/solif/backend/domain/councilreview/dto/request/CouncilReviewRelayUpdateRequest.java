package com.solif.backend.domain.councilreview.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "자치회 활동 후기 릴레이 수정 요청")
public class CouncilReviewRelayUpdateRequest {

    @NotBlank(message = "릴레이 내용은 필수입니다.")
    @Schema(description = "수정할 릴레이 내용", example = "수정된 릴레이 내용...")
    private String relayContent;
}