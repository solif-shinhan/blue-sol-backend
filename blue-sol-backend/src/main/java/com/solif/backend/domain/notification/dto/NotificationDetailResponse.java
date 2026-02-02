package com.solif.backend.domain.notification.dto;

import com.solif.backend.domain.notification.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "알림 상세 응답")
public class NotificationDetailResponse {

    @Schema(description = "알림 ID", example = "1")
    private Long notificationId;

    @Schema(description = "알림 제목")
    private String notificationTitle;

    @Schema(description = "알림 내용")
    private String notificationContent;

    @Schema(description = "알림 종류", example = "NOTICE")
    private String notificationType;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;

    @Schema(description = "첨부 이미지 목록")
    private List<ImageInfo> images;

    @Schema(description = "이동 대상 타입", example = "MESSAGE")
    private String targetType;

    @Schema(description = "이동 대상 ID", example = "10")
    private Long targetId;

    @Getter
    @Builder
    @Schema(description = "이미지 정보")
    public static class ImageInfo {

        @Schema(description = "이미지 파일 ID", example = "1")
        private Long imageId;

        @Schema(description = "이미지 URL", example = "https://cdn.solid.com/images/...")
        private String imageUrl;
    }

    public static NotificationDetailResponse from(Notification notification, List<ImageInfo> images) {
        return NotificationDetailResponse.builder()
                .notificationId(notification.getNotificationId())
                .notificationTitle(notification.getNotificationTitle())
                .notificationContent(notification.getNotificationContent())
                .notificationType(notification.getNotificationType().name())
                .createdAt(notification.getCreatedAt())
                .images(images)
                .targetType(notification.getTargetType().name())
                .targetId(notification.getTargetId())
                .build();
    }
}
