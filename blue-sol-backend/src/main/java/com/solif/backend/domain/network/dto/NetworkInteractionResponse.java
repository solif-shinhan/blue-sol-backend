package com.solif.backend.domain.network.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "상호작용 발송 응답")
public class NetworkInteractionResponse {

    @Schema(description = "알림 ID")
    private Long notificationId;

    @Schema(description = "대상 사용자 ID")
    private Long targetUserId;

    @Schema(description = "상호작용 타입")
    private String interactionType;

    @Schema(description = "알림 내용")
    private String notificationContent;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;
}
