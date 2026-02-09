package com.solif.backend.domain.message.service;

import com.solif.backend.domain.file.entity.AttachmentPurpose;
import com.solif.backend.domain.file.entity.FileAttachment;
import com.solif.backend.domain.file.entity.FileTargetType;
import com.solif.backend.domain.file.repository.FileAttachmentRepository;
import com.solif.backend.domain.file.service.FileService;
import com.solif.backend.domain.notification.entity.NotificationType;
import com.solif.backend.domain.notification.entity.TargetType;
import com.solif.backend.domain.notification.event.NotificationEvent;
import com.solif.backend.domain.auth.code.AuthErrorCode;
import com.solif.backend.domain.message.code.MessageErrorCode;
import com.solif.backend.domain.message.dto.*;
import com.solif.backend.domain.message.entity.Message;
import com.solif.backend.domain.message.repository.MessageRepository;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final FileService fileService;
    private final FileAttachmentRepository fileAttachmentRepository;

    @Value("${cloud.aws.region.static}")
    private String region;

    // 쪽지 발송
    @Transactional
    public MessageSendResponse sendMessage(Long senderId, MessageSendRequest request) {
        log.info("쪽지 발송 - senderId: {}, receiverId: {}", senderId, request.getReceiverId());

        // 자기 자신에게 보내기 방지
        if (senderId.equals(request.getReceiverId())) {
            throw new CustomException(MessageErrorCode.CANNOT_SEND_TO_SELF);
        }

        // 발신자 조회
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 수신자 조회
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new CustomException(MessageErrorCode.RECEIVER_NOT_FOUND));

        // 메시지 생성
        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .messageTitle(request.getMessageTitle())
                .messageContent(request.getMessageContent())
                .build();

        Message savedMessage = messageRepository.save(message);

        // 파일 확정 (fileIds가 있으면)
        if (request.getFileIds() != null && !request.getFileIds().isEmpty()) {
            fileService.confirmFiles(
                    request.getFileIds(),
                    FileTargetType.MESSAGE,
                    savedMessage.getMessageId(),
                    AttachmentPurpose.MESSAGE_ATTACHMENT
            );
        }

        // 수신자에게 알림 생성 + SSE 실시간 전송 (트랜잭션 커밋 후 이벤트 리스너에서 처리)
        eventPublisher.publishEvent(new NotificationEvent(
                request.getReceiverId(),
                NotificationType.MESSAGE,
                TargetType.MESSAGE,
                savedMessage.getMessageId(),
                "새로운 쪽지가 도착했습니다.",
                savedMessage.getMessageTitle()
        ));

        return MessageSendResponse.from(savedMessage);
    }

    // 받은 쪽지 목록 조회
    public Slice<MessageListResponse> getReceivedMessages(Long userId, Pageable pageable) {
        log.info("받은 쪽지 목록 조회 - userId: {}", userId);

        Slice<Message> messages = messageRepository.findReceivedMessages(userId, pageable);

        return messages.map(MessageListResponse::fromReceived);
    }

    // 보낸 쪽지 목록 조회
    public Slice<MessageListResponse> getSentMessages(Long userId, Pageable pageable) {
        log.info("보낸 쪽지 목록 조회 - userId: {}", userId);

        Slice<Message> messages = messageRepository.findSentMessages(userId, pageable);

        return messages.map(MessageListResponse::fromSent);
    }

    // 쪽지 상세 조회
    @Transactional
    public MessageDetailResponse getMessageDetail(Long userId, Long messageId) {
        log.info("쪽지 상세 조회 - userId: {}, messageId: {}", userId, messageId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(MessageErrorCode.MESSAGE_NOT_FOUND));

        // 접근 권한 확인 (발신자 또는 수신자만 조회 가능)
        if (!message.canAccess(userId)) {
            throw new CustomException(MessageErrorCode.UNAUTHORIZED_MESSAGE_ACCESS);
        }

        // 수신자가 조회하는 경우 읽음 처리
        if (message.isReceiver(userId)) {
            // 이미 삭제된 쪽지인지 확인
            if (message.getDeletedByReceiver()) {
                throw new CustomException(MessageErrorCode.ALREADY_DELETED_MESSAGE);
            }
            message.markAsRead();
        }

        // 발신자가 조회하는 경우 삭제 여부 확인
        if (message.isSender(userId) && message.getDeletedBySender()) {
            throw new CustomException(MessageErrorCode.ALREADY_DELETED_MESSAGE);
        }

        // 첨부 파일 URL 조회
        List<FileAttachment> attachments = fileAttachmentRepository
                .findByFileTargetTypeAndTargetIdOrderBySortOrder(FileTargetType.MESSAGE, messageId);

        List<String> imageUrls = attachments.stream()
                .map(attachment -> attachment.getFile().getUrl(region))
                .toList();

        return MessageDetailResponse.from(message, imageUrls);
    }

    // 쪽지 삭제
    @Transactional
    public void deleteMessage(Long userId, Long messageId) {
        log.info("쪽지 삭제 - userId: {}, messageId: {}", userId, messageId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(MessageErrorCode.MESSAGE_NOT_FOUND));

        // 접근 권한 확인
        if (!message.canAccess(userId)) {
            throw new CustomException(MessageErrorCode.UNAUTHORIZED_MESSAGE_ACCESS);
        }

        // 발신자/수신자에 따라 삭제 처리
        if (message.isSender(userId)) {
            if (message.getDeletedBySender()) {
                throw new CustomException(MessageErrorCode.ALREADY_DELETED_MESSAGE);
            }
            message.deleteBySender();
        }

        if (message.isReceiver(userId)) {
            if (message.getDeletedByReceiver()) {
                throw new CustomException(MessageErrorCode.ALREADY_DELETED_MESSAGE);
            }
            message.deleteByReceiver();
        }
    }
}
