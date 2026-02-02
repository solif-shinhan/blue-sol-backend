package com.solif.backend.domain.notification.dto;

import com.solif.backend.domain.notification.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "알림 목록 응답")
public class NotificationListResponse {

    @Schema(description = "알림 ID", example = "1")
    private Long notificationId;

    @Schema(description = "알림 종류", example = "MESSAGE")
    private String notificationType;

    @Schema(description = "알림 제목", example = "[공지] 2026년도 신규 장학생 모집 시작!")
    private String notificationTitle;

    @Schema(description = "알림 내용", example = "꿈을 향한 여러분의 도전을 응원합니다.")
    private String notificationContent;

    @Schema(description = "이동 대상 타입", example = "MESSAGE")
    private String targetType;

    @Schema(description = "이동 대상 ID", example = "10")
    private Long targetId;

    @Schema(description = "읽음 여부", example = "false")
    private Boolean isRead;

    @Schema(description = "생성 일시", example = "2026-01-31T12:00:00")
    private LocalDateTime createdAt;

    public static NotificationListResponse from(Notification notification) {
        return NotificationListResponse.builder()
                .notificationId(notification.getNotificationId())
                .notificationType(notification.getNotificationType().name())
                .notificationTitle(notification.getNotificationTitle())
                .notificationContent(notification.getNotificationContent())
                .targetType(notification.getTargetType().name())
                .targetId(notification.getTargetId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
