package com.solif.backend.domain.council.entity;

import com.solif.backend.domain.council.code.CouncilErrorCode;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.global.common.exception.CustomException;
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
        // 사용한 예산 계산
        Long usedBudget = this.totalBudget - this.currentBudget;

        // 총 예산이 줄어들었을 때, 이미 사용한 예산보다 작으면 에러
        if (totalBudget < usedBudget) {
            throw new CustomException(CouncilErrorCode.BUDGET_REDUCTION_NOT_ALLOWED);
        }

        // 총 예산 변경에 따른 남은 예산 조정
        this.currentBudget = totalBudget - usedBudget;

        this.councilName = councilName;
        this.region = region;
        this.activityCategory = activityCategory;
        this.description = description;
        this.totalBudget = totalBudget;
        this.profileImageFileId = profileImageFileId;
    }

    // 예산 차감 (활동 후기 작성 시)
    public void decreaseBudget(Long usedBudget) {
        if (usedBudget == null || usedBudget < 0) {
            throw new CustomException(CouncilErrorCode.INVALID_BUDGET_AMOUNT);
        }

        // 예산 차감
        this.currentBudget = this.currentBudget - usedBudget;

        // 예산이 음수가 되면 0원으로 표시 (예산 초과 허용)
        if (this.currentBudget < 0) {
            this.currentBudget = 0L;
        }
    }

    // 예산 복구 (활동 후기 수정/삭제 시)
    public void increaseBudget(Long refundBudget) {
        if (refundBudget == null || refundBudget < 0) {
            throw new CustomException(CouncilErrorCode.INVALID_BUDGET_AMOUNT);
        }

        // 복구는 총 예산을 초과할 수 없음
        this.currentBudget = Math.min(this.totalBudget, this.currentBudget + refundBudget);
    }

    public boolean isLeader(Long userId) {
        return this.leader.getUserId().equals(userId);
    }
}