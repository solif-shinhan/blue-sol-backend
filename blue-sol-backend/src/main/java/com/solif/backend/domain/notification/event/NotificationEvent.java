package com.solif.backend.domain.notification.event;

import com.solif.backend.domain.notification.entity.NotificationType;
import com.solif.backend.domain.notification.entity.NotificationTargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 알림 발송 이벤트.
 * 트랜잭션 커밋 이후에 리스너가 수신하여 알림을 생성합니다.
 */
@Getter
@AllArgsConstructor
public class NotificationEvent {

    private final Long receiverUserId;
    private final NotificationType notificationType;
    private final NotificationTargetType notificationTargetType;
    private final Long targetId;
    private final String title;
    private final String content;
}
