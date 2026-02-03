package com.solif.backend.domain.profile.entity;

import com.solif.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile")
@Getter
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "user_character", nullable = false, length = 255)
    private String userCharacter;

    @Column(name = "background_pattern", nullable = false, length = 255)
    private String backgroundPattern;

    @Column(name = "main_goal", nullable = false, columnDefinition = "TEXT")
    private String mainGoal;

    @Column(name = "solid_goal_name", nullable = false, columnDefinition = "TEXT")
    private String solidGoalName;

    @Column(name = "qr_code_data", nullable = false, unique = true, columnDefinition = "TEXT")
    private String qrCodeData;

    @Column(name = "qr_image_url", columnDefinition = "TEXT")
    private String qrImageUrl;

    @Column(name = "complete_goal_count", nullable = false)
    private Integer completeGoalCount = 0;

    @Column(name = "connection_count", nullable = false)
    private Integer connectionCount = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public UserProfile(User user, String userCharacter, String backgroundPattern,
                       String mainGoal, String solidGoalName, String qrCodeData, String qrImageUrl) {
        this.user = user;
        this.userCharacter = userCharacter;
        this.backgroundPattern = backgroundPattern;
        this.mainGoal = mainGoal;
        this.solidGoalName = solidGoalName;
        this.qrCodeData = qrCodeData;
        this.qrImageUrl = qrImageUrl;
        this.completeGoalCount = 0;
        this.connectionCount = 0;
    }

    public void update(String mainGoal, String userCharacter, String solidGoalName, String backgroundPattern) {
        if (mainGoal != null) this.mainGoal = mainGoal;
        if (userCharacter != null) this.userCharacter = userCharacter;
        if (solidGoalName != null) this.solidGoalName = solidGoalName;
        if (backgroundPattern != null) this.backgroundPattern = backgroundPattern;
    }

    public void incrementConnectionCount() {
        this.connectionCount++;
    }
}
