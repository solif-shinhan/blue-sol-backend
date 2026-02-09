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
@Table(name = "mentoring_interaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MentoringInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mentoring_interaction_id")
    private Long mentoringInteractionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_user_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_user_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false, length = 50)
    private MentoringInteractionType interactionType;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public MentoringInteraction(User sender, User receiver, MentoringInteractionType interactionType) {
        this.sender = sender;
        this.receiver = receiver;
        this.interactionType = interactionType;
    }
}