package com.solif.backend.domain.notification.entity;

import java.util.List;

/**
 * 활동 탭 내 서브 필터 (쪽지 / 교류 / 자치회 활동)
 * category=ACTIVITY 일 때만 사용됩니다.
 */
public enum NotificationSubCategory {
    MESSAGE,    // 쪽지
    NETWORK,    // 교류 (응원하기, 도와줄게)
    COUNCIL;    // 자치회 활동

    public List<NotificationType> getTypes() {
        return switch (this) {
            case MESSAGE -> List.of(NotificationType.MESSAGE);
            case NETWORK -> List.of(NotificationType.CHEER, NotificationType.HELP);
            case COUNCIL -> List.of(NotificationType.COUNCIL_ACTIVITY);
        };
    }
}
