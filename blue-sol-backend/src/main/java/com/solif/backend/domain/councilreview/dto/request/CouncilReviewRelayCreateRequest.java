package com.solif.backend.domain.councilreview.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "자치회 활동 후기 릴레이 작성 요청")
public class CouncilReviewRelayCreateRequest {

    @NotNull(message = "질문 ID는 필수입니다.")
    @Schema(description = "선택한 질문 ID", example = "7")
    private Long questionId;

    @NotBlank(message = "릴레이 내용은 필수입니다.")
    @Schema(description = "릴레이 내용", example = "가장 즐거웠던 순간은 친구들과 공유할 즐거움을 알려주세요...")
    private String relayContent;
}