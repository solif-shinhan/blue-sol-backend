package com.solif.backend.domain.mentoring.entity;

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
@Table(name = "mentoring_card")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MentoringCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mentoring_card_id")
    private Long mentoringCardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_user_id", nullable = false)
    private User sender;

    @Column(name = "card_title", nullable = false, length = 255)
    private String cardTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private MentoringCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 50)
    private MentoringMethod method;

    @Column(name = "card_content", nullable = false, columnDefinition = "TEXT")
    private String cardContent;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public MentoringCard(User sender, String cardTitle, MentoringCategory category,
                         MentoringMethod method, String cardContent) {
        this.sender = sender;
        this.cardTitle = cardTitle;
        this.category = category;
        this.method = method;
        this.cardContent = cardContent;
        this.isRead = false;
    }

    // 비즈니스 로직
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }

    public boolean isSender(Long userId) {
        return this.sender.getUserId().equals(userId);
    }
}