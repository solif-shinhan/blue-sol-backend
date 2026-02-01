package com.solif.backend.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "안읽은 알림 개수 응답")
public class UnreadCountResponse {

    @Schema(description = "안읽은 알림 개수", example = "12")
    private long unreadCount;
}
