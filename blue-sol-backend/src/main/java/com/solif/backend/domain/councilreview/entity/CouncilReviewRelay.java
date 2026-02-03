package com.solif.backend.domain.councilreview.entity;

import com.solif.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "council_review_relay")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CouncilReviewRelay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "council_review_relay_id")
    private Long councilReviewRelayId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "council_review_post_id", nullable = false)
    private CouncilReviewPost councilReviewPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "council_review_question_id", nullable = false)
    private CouncilReviewQuestion councilReviewQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_user_id", nullable = false)
    private User writerUser;

    @Column(name = "relay_order", nullable = false)
    private Integer relayOrder;

    @Column(name = "relay_content", nullable = false, columnDefinition = "TEXT")
    private String relayContent;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public CouncilReviewRelay(CouncilReviewPost councilReviewPost,
                              CouncilReviewQuestion councilReviewQuestion,
                              User writerUser, Integer relayOrder,
                              String relayContent) {
        this.councilReviewPost = councilReviewPost;
        this.councilReviewQuestion = councilReviewQuestion;
        this.writerUser = writerUser;
        this.relayOrder = relayOrder;
        this.relayContent = relayContent;
    }

    // 비즈니스 로직
    public void updateRelayContent(String relayContent) {
        this.relayContent = relayContent;
    }

    public void updateRelayOrder(Integer relayOrder) {
        this.relayOrder = relayOrder;
    }

    public boolean isWriter(Long userId) {
        return this.writerUser.getUserId().equals(userId);
    }

    public Long getQuestionId() {
        return this.councilReviewQuestion.getCouncilReviewQuestionId();
    }
}