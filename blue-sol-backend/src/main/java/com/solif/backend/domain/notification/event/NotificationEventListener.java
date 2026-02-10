package com.solif.backend.domain.notification.event;

import com.solif.backend.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 알림 이벤트 리스너.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleNotificationEvent(NotificationEvent event) {
        log.info(">>> 알림 이벤트 수신됨 - type: {}, receiverId: {}, targetType: {}",
                event.getNotificationType(), event.getReceiverUserId(), event.getNotificationTargetType());
        try {
            notificationService.send(
                    event.getReceiverUserId(),
                    event.getNotificationType(),
                    event.getNotificationTargetType(),
                    event.getTargetId(),
                    event.getTitle(),
                    event.getContent()
            );
        } catch (Exception e) {
            log.warn("알림 발송 실패 - targetType: {}, targetId: {}, receiverId: {}, error: {}",
                    event.getNotificationTargetType(), event.getTargetId(),
                    event.getReceiverUserId(), e.getMessage());
        }
    }
}
