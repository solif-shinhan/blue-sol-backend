package com.solif.backend.domain.network.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "상호작용 발송 요청")
public class NetworkInteractionRequest {

    @NotNull(message = "대상 사용자 ID는 필수입니다.")
    @Schema(description = "대상 사용자 ID")
    private Long targetUserId;

    @NotBlank(message = "상호작용 타입은 필수입니다.")
    @Schema(description = "상호작용 타입", example = "CHEER")
    private String interactionType;
}
