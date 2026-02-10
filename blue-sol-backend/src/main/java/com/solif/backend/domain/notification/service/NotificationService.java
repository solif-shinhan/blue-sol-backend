package com.solif.backend.domain.notification.service;

import com.solif.backend.domain.auth.code.AuthErrorCode;
import com.solif.backend.domain.message.entity.Message;
import com.solif.backend.domain.message.repository.MessageRepository;
import com.solif.backend.domain.notification.code.NotificationErrorCode;
import com.solif.backend.domain.notification.dto.*;
import com.solif.backend.domain.notification.entity.*;
import com.solif.backend.domain.notification.repository.NotificationRepository;
import com.solif.backend.domain.notification.repository.SseEmitterRepository;
import com.solif.backend.domain.profile.entity.UserProfile;
import com.solif.backend.domain.profile.repository.UserProfileRepository;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final Long SSE_TIMEOUT = 60L * 60 * 1000; // 1시간

    private final NotificationRepository notificationRepository;
    private final SseEmitterRepository sseEmitterRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final UserProfileRepository userProfileRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    //  SSE 연결

    //SSE 구독 (클라이언트가 앱 진입 시 호출)
    public SseEmitter subscribe(Long userId) {
        String emitterId = userId + "_" + UUID.randomUUID();

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        sseEmitterRepository.save(emitterId, emitter);

        // 콜백 등록: 타임아웃/에러/완료 시 emitter 제거
        emitter.onCompletion(() -> sseEmitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> sseEmitterRepository.deleteById(emitterId));
        emitter.onError(e -> sseEmitterRepository.deleteById(emitterId));

        // 연결 직후 더미 이벤트 전송 (503 방지)
        sendToEmitter(emitter, emitterId, "connect", "SSE 연결 성공 [userId=" + userId + "]");

        return emitter;
    }

    //  알림 생성 + SSE 전송 (다른 서비스에서 호출)

     //알림 생성 후 실시간 전송
     //다른 도메인 서비스(MessageService 등)에서 호출합니다.
    @Transactional
    public Notification send(Long receiverUserId, NotificationType type, NotificationTargetType notificationTargetType,
                             Long targetId, String title, String content) {

        User receiver = userRepository.findById(receiverUserId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 1. DB 저장
        Notification notification = Notification.builder()
                .receiver(receiver)
                .notificationType(type)
                .notificationTargetType(notificationTargetType)
                .targetId(targetId)
                .notificationTitle(title)
                .notificationContent(content)
                .build();

        Notification saved = notificationRepository.save(notification);

        // 2. SSE 실시간 전송
        sendSseToUser(receiverUserId, saved);

        return saved;
    }

    //  REST API: 알림 목록 조회

    //알림 목록 조회 (카테고리 + 서브카테고리 + 필터)
    public Slice<NotificationListResponse> getNotifications(Long userId, NotificationCategory category,
                                                             NotificationSubCategory subCategory,
                                                             String filter, Pageable pageable) {
        log.info("알림 목록 조회 - userId: {}, category: {}, subCategory: {}, filter: {}",
                userId, category, subCategory, filter);

        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        List<NotificationType> types = category.getTypes(subCategory);

        Slice<Notification> notifications;
        if ("UNREAD".equalsIgnoreCase(filter)) {
            notifications = notificationRepository
                    .findByReceiverAndNotificationTypeInAndIsReadFalseOrderByCreatedAtDesc(
                            receiver, types, pageable);
        } else {
            notifications = notificationRepository
                    .findByReceiverAndNotificationTypeInOrderByCreatedAtDesc(
                            receiver, types, pageable);
        }

        // MESSAGE 타입 알림의 발신자 정보 배치 조회
        List<Long> messageTargetIds = notifications.getContent().stream()
                .filter(n -> n.getNotificationType() == NotificationType.MESSAGE)
                .map(Notification::getTargetId)
                .distinct()
                .toList();

        Map<Long, Message> messageMap = messageTargetIds.isEmpty()
                ? Collections.emptyMap()
                : messageRepository.findAllByIdWithSender(messageTargetIds).stream()
                    .collect(java.util.stream.Collectors.toMap(Message::getMessageId, m -> m));

        // CHEER, HELP, CONNECTION 타입은 targetId가 sender userId임
        List<Long> userTargetIds = notifications.getContent().stream()
                .filter(n -> n.getNotificationType() == NotificationType.CHEER
                        || n.getNotificationType() == NotificationType.HELP
                        || n.getNotificationType() == NotificationType.CONNECTION)
                .map(Notification::getTargetId)
                .distinct()
                .toList();

        Map<Long, User> userMap = userTargetIds.isEmpty()
                ? Collections.emptyMap()
                : userRepository.findAllById(userTargetIds).stream()
                    .collect(java.util.stream.Collectors.toMap(User::getUserId, u -> u));

        // 발신자들의 프로필 이미지 배치 조회 (MESSAGE 발신자 + CHEER/HELP/CONNECTION 발신자)
        List<Long> allSenderUserIds = new java.util.ArrayList<>();
        messageMap.values().stream()
                .map(m -> m.getSender().getUserId())
                .distinct()
                .forEach(allSenderUserIds::add);
        allSenderUserIds.addAll(userTargetIds);

        Map<Long, String> profileImageMap = allSenderUserIds.isEmpty()
                ? Collections.emptyMap()
                : userProfileRepository.findByUser_UserIdIn(allSenderUserIds.stream().distinct().toList()).stream()
                    .filter(profile -> profile.getUserCharacter() != null)
                    .collect(java.util.stream.Collectors.toMap(
                            profile -> profile.getUser().getUserId(),
                            profile -> String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, profile.getUserCharacter()),
                            (existing, replacement) -> existing
                    ));

        return notifications.map(notification -> {
            if (notification.getNotificationType() == NotificationType.MESSAGE) {
                Message message = messageMap.get(notification.getTargetId());
                if (message != null) {
                    String senderName = message.getSender().getName();
                    String senderProfileImage = profileImageMap.get(message.getSender().getUserId());
                    return NotificationListResponse.from(notification, senderName, senderProfileImage);
                }
            } else if (notification.getNotificationType() == NotificationType.CHEER
                    || notification.getNotificationType() == NotificationType.HELP
                    || notification.getNotificationType() == NotificationType.CONNECTION) {
                User sender = userMap.get(notification.getTargetId());
                if (sender != null) {
                    String senderProfileImage = profileImageMap.get(sender.getUserId());
                    return NotificationListResponse.from(notification, sender.getName(), senderProfileImage);
                }
            }
            return NotificationListResponse.from(notification);
        });
    }

    //  REST API: 알림 읽음 처리

    //알림 읽음 처리
    @Transactional
    public NotificationReadResponse markAsRead(Long userId, Long notificationId) {
        log.info("알림 읽음 처리 - userId: {}, notificationId: {}", userId, notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        // 본인 알림인지 확인
        validateOwner(userId, notification);

        notification.markAsRead();

        return NotificationReadResponse.from(notification);
    }

    //  REST API: 안읽은 알림 개수

    //안읽은 알림 개수 조회
    public UnreadCountResponse getUnreadCount(Long userId) {
        log.info("안읽은 알림 개수 조회 - userId: {}", userId);

        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        long count = notificationRepository.countByReceiverAndIsReadFalse(receiver);

        return new UnreadCountResponse(count);
    }

    //  REST API: 알림 상세 조회

    //알림 상세 조회
    public NotificationDetailResponse getNotificationDetail(Long userId, Long notificationId) {
        log.info("알림 상세 조회 - userId: {}, notificationId: {}", userId, notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        validateOwner(userId, notification);

        // TODO: 알림의 targetType/targetId 기반으로 첨부 이미지 조회
        List<NotificationDetailResponse.ImageInfo> images = Collections.emptyList();

        // 발신자 정보 조회
        String senderName = null;
        String senderProfileImage = null;

        if (notification.getNotificationType() == NotificationType.MESSAGE) {
            // 쪽지 알림: Message 엔티티에서 발신자 조회
            Message message = messageRepository.findById(notification.getTargetId()).orElse(null);
            if (message != null) {
                senderName = message.getSender().getName();
                senderProfileImage = getCharacterImageUrl(message.getSender().getUserId());
            }
        } else if (notification.getNotificationType() == NotificationType.CHEER
                || notification.getNotificationType() == NotificationType.HELP
                || notification.getNotificationType() == NotificationType.CONNECTION) {
            // CHEER/HELP/CONNECTION 알림: targetId가 sender userId
            User sender = userRepository.findById(notification.getTargetId()).orElse(null);
            if (sender != null) {
                senderName = sender.getName();
                senderProfileImage = getCharacterImageUrl(sender.getUserId());
            }
        }

        return NotificationDetailResponse.from(notification, images, senderName, senderProfileImage);
    }

    /**
     * 사용자 ID로 캐릭터 이미지 URL을 생성합니다.
     */
    private String getCharacterImageUrl(Long userId) {
        return userProfileRepository.findByUser_UserId(userId)
                .filter(profile -> profile.getUserCharacter() != null)
                .map(profile -> String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, profile.getUserCharacter()))
                .orElse(null);
    }

    //  Private 메서드

    //특정 유저에게 SSE 알림 전송
    private void sendSseToUser(Long userId, Notification notification) {
        Map<String, SseEmitter> emitters = sseEmitterRepository.findAllByUserId(userId);

        NotificationSseResponse data = NotificationSseResponse.from(notification);

        emitters.forEach((emitterId, emitter) -> {
            sendToEmitter(emitter, emitterId, "notification", data);
        });
    }

    //개별 Emitter에 이벤트 전송
    private void sendToEmitter(SseEmitter emitter, String emitterId, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(emitterId)
                    .name(eventName)
                    .data(data));
        } catch (IOException | IllegalStateException e) {
            log.warn("SSE 전송 실패 - emitterId: {}, 제거합니다.", emitterId);
            sseEmitterRepository.deleteById(emitterId);
        }
    }

    //알림 소유자 검증
    private void validateOwner(Long userId, Notification notification) {
        if (!notification.getReceiver().getUserId().equals(userId)) {
            throw new CustomException(NotificationErrorCode.UNAUTHORIZED_NOTIFICATION_ACCESS);
        }
    }
}
