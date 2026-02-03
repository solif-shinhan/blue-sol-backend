package com.solif.backend.domain.councilreview.entity;

import com.solif.backend.domain.council.entity.Council;
import com.solif.backend.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "council_review_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CouncilReviewPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "council_review_post_id")
    private Long councilReviewPostId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "council_id", nullable = false)
    private Council council;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "activity_location", nullable = false, length = 255)
    private String activityLocation;

    @Column(name = "total_cost", nullable = false)
    private Long totalCost = 0L;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public CouncilReviewPost(Post post, Council council, LocalDate activityDate,
                             String activityLocation, Long totalCost) {
        this.post = post;
        this.council = council;
        this.activityDate = activityDate;
        this.activityLocation = activityLocation;
        this.totalCost = totalCost != null ? totalCost : 0L;
    }

    // 비즈니스 로직
    public void updateReviewPost(String postTitle, LocalDate activityDate,
                                 String activityLocation, Long totalCost) {
        // Post 제목 업데이트
        this.post.updatePost(postTitle, this.post.getPostContent());

        // 활동 정보 업데이트
        this.activityDate = activityDate;
        this.activityLocation = activityLocation;
        this.totalCost = totalCost;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.post.softDelete();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isLeader(Long userId) {
        return this.council.isLeader(userId);
    }

    public Long getOldTotalCost() {
        return this.totalCost;
    }
}