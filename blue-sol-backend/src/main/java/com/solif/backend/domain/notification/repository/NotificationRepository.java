package com.solif.backend.domain.notification.repository;

import com.solif.backend.domain.notification.entity.Notification;
import com.solif.backend.domain.notification.entity.NotificationType;
import com.solif.backend.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 카테고리별 전체 조회 (페이징)
    Slice<Notification> findByReceiverAndNotificationTypeInOrderByCreatedAtDesc(
            User receiver, List<NotificationType> types, Pageable pageable);

    // 카테고리별 안읽은 알림만 조회 (페이징)
    Slice<Notification> findByReceiverAndNotificationTypeInAndIsReadFalseOrderByCreatedAtDesc(
            User receiver, List<NotificationType> types, Pageable pageable);

    // 안읽은 알림 수
    long countByReceiverAndIsReadFalse(User receiver);

    // 전체 안읽은 알림 목록 (카테고리 무관)
    Slice<Notification> findByReceiverAndIsReadFalseOrderByCreatedAtDesc(
            User receiver, Pageable pageable);

    // targetId로 조회 (targetId에 sender userId 저장됨)
    List<Notification> findByNotificationTypeAndTargetId(NotificationType type, Long targetId);
}
