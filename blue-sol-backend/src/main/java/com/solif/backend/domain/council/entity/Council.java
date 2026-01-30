package com.solif.backend.domain.council.entity;

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
@Table(name = "council")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Council {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "council_id")
    private Long councilId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_user_id", nullable = false)
    private User leader;

    @Column(name = "council_name", nullable = false, length = 100)
    private String councilName;

    @Column(name = "region", length = 50)
    private String region;  // 활동 지역

    @Column(name = "activity_category", length = 50)
    private String activityCategory;  // 활동 주제

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_budget", nullable = false)
    private Long totalBudget = 0L;

    @Column(name = "current_budget", nullable = false)
    private Long currentBudget = 0L;

    @Column(name = "profile_image_file_id")
    private Long profileImageFileId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Council(User leader, String councilName, String region,
                   String activityCategory, String description,
                   Long totalBudget, Long profileImageFileId) {
        this.leader = leader;
        this.councilName = councilName;
        this.region = region;
        this.activityCategory = activityCategory;
        this.description = description;
        this.totalBudget = totalBudget != null ? totalBudget : 0L;
        this.currentBudget = totalBudget != null ? totalBudget : 0L;
        this.profileImageFileId = profileImageFileId;
    }

    // 비즈니스 로직
    public void updateCouncil(String councilName, String region,
                              String activityCategory, String description,
                              Long totalBudget, Long profileImageFileId) {
        this.councilName = councilName;
        this.region = region;
        this.activityCategory = activityCategory;
        this.description = description;
        this.totalBudget = totalBudget;
        this.profileImageFileId = profileImageFileId;
    }

    public void decreaseBudget(Long usedBudget) {
        if (this.currentBudget < usedBudget) {
            throw new IllegalArgumentException("예산이 부족합니다.");
        }
        this.currentBudget -= usedBudget;
    }

    public boolean isLeader(Long userId) {
        return this.leader.getUserId().equals(userId);
    }
}