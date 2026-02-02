package com.solif.backend.domain.notification.dto;

import com.solif.backend.domain.notification.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "알림 읽음 처리 응답")
public class NotificationReadResponse {

    @Schema(description = "알림 ID", example = "1")
    private Long notificationId;

    @Schema(description = "읽은 시각", example = "2026-01-31T12:30:00")
    private LocalDateTime readAt;

    public static NotificationReadResponse from(Notification notification) {
        return NotificationReadResponse.builder()
                .notificationId(notification.getNotificationId())
                .readAt(notification.getReadAt())
                .build();
    }
}
