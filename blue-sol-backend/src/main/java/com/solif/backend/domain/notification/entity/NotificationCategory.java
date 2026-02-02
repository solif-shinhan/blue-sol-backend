package com.solif.backend.domain.notification.entity;

import java.util.List;

/**
 * 알림 목록 조회 시 카테고리 필터용 (공지사항 / 활동 탭 구분)
 */
public enum NotificationCategory {
    NOTICE,     // 공지사항 탭
    ACTIVITY;   // 활동 탭

    /**
     * 카테고리 + 서브카테고리 기반으로 NotificationType 목록 반환
     *
     * @param subCategory 활동 탭 내 서브 필터 (null이면 활동 전체)
     */
    public List<NotificationType> getTypes(NotificationSubCategory subCategory) {
        if (this == NOTICE) {
            return List.of(NotificationType.NOTICE);
        }

        // ACTIVITY인 경우
        if (subCategory != null) {
            return subCategory.getTypes();
        }

        // 서브카테고리 없으면 활동 전체
        return List.of(
                NotificationType.COMMENT,
                NotificationType.MESSAGE,
                NotificationType.CHEER,
                NotificationType.HELP,
                NotificationType.COUNCIL_ACTIVITY
        );
    }
}
