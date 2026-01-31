package com.solif.backend.domain.message.entity;

import com.solif.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(name = "message_title", nullable = false, length = 255)
    private String messageTitle;

    @Column(name = "message_content", nullable = false, columnDefinition = "TEXT")
    private String messageContent;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "deleted_by_sender", nullable = false)
    private Boolean deletedBySender = false;

    @Column(name = "deleted_by_receiver", nullable = false)
    private Boolean deletedByReceiver = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Message(User sender, User receiver, String messageTitle, String messageContent) {
        this.sender = sender;
        this.receiver = receiver;
        this.messageTitle = messageTitle;
        this.messageContent = messageContent;
        this.isRead = false;
        this.deletedBySender = false;
        this.deletedByReceiver = false;
    }

    // 비즈니스 로직
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }

    public void deleteBySender() {
        this.deletedBySender = true;
    }

    public void deleteByReceiver() {
        this.deletedByReceiver = true;
    }

    public boolean isSender(Long userId) {
        return this.sender.getUserId().equals(userId);
    }

    public boolean isReceiver(Long userId) {
        return this.receiver.getUserId().equals(userId);
    }

    public boolean canAccess(Long userId) {
        return isSender(userId) || isReceiver(userId);
    }
}
