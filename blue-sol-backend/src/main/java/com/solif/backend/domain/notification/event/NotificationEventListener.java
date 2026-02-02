package com.solif.backend.domain.notification.event;

import com.solif.backend.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 알림 이벤트 리스너.
 * 호출자의 트랜잭션이 커밋된 후(AFTER_COMMIT)에만 알림을 발송합니다.
 * - 고아 알림 방지: 메시지가 롤백되면 알림도 발송되지 않음
 * - 알림 실패가 메시지 저장에 영향을 주지 않음
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(NotificationEvent event) {
        try {
            notificationService.send(
                    event.getReceiverUserId(),
                    event.getNotificationType(),
                    event.getTargetType(),
                    event.getTargetId(),
                    event.getTitle(),
                    event.getContent()
            );
        } catch (Exception e) {
            log.warn("알림 발송 실패 - targetType: {}, targetId: {}, receiverId: {}, error: {}",
                    event.getTargetType(), event.getTargetId(),
                    event.getReceiverUserId(), e.getMessage());
        }
    }
}
