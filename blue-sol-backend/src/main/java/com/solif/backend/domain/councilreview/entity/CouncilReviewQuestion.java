package com.solif.backend.domain.councilreview.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "council_review_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CouncilReviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "council_review_question_id")
    private Long councilReviewQuestionId;

    @Column(name = "question_text", nullable = false, length = 255)
    private String questionText;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public CouncilReviewQuestion(String questionText, Boolean isActive) {
        this.questionText = questionText;
        this.isActive = isActive != null ? isActive : true;
    }

    // 비즈니스 로직
    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public boolean isActive() {
        return this.isActive;
    }
}