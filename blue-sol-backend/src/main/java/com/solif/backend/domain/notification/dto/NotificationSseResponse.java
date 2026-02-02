package com.solif.backend.domain.notification.dto;

import com.solif.backend.domain.notification.entity.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

//SSE로 실시간 전송할 알림 데이터
@Getter
@Builder
public class NotificationSseResponse {

    private Long notificationId;
    private String notificationType;
    private String notificationTitle;
    private String notificationContent;
    private String targetType;
    private Long targetId;
    private LocalDateTime createdAt;

    public static NotificationSseResponse from(Notification notification) {
        return NotificationSseResponse.builder()
                .notificationId(notification.getNotificationId())
                .notificationType(notification.getNotificationType().name())
                .notificationTitle(notification.getNotificationTitle())
                .notificationContent(notification.getNotificationContent())
                .targetType(notification.getTargetType().name())
                .targetId(notification.getTargetId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
