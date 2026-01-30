package com.solif.backend.domain.notification.repository;

import com.solif.backend.domain.notification.entity.Notification;
import com.solif.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 사용자의 알림 목록 조회
    List<Notification> findAllByReceiverOrderByCreatedAtDesc(User receiver);

    // 읽지 않은 알림 수
    long countByReceiverAndIsReadFalse(User receiver);
}
