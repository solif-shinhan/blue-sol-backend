package com.solif.backend.domain.councilreview.entity;

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
@Table(
        name = "council_review_participant",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_council_review_participant",
                        columnNames = {"council_review_post_id", "user_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CouncilReviewParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "council_review_participant_id")
    private Long councilReviewParticipantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "council_review_post_id", nullable = false)
    private CouncilReviewPost councilReviewPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public CouncilReviewParticipant(CouncilReviewPost councilReviewPost, User user) {
        this.councilReviewPost = councilReviewPost;
        this.user = user;
    }

    // 비즈니스 로직
    public boolean isParticipant(Long userId) {
        return this.user.getUserId().equals(userId);
    }
}