package com.solif.backend.domain.mission.entity;

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
@Table(name = "user_pinecone",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_pinecone_season",
                columnNames = {"user_id", "pinecone_category", "season_key"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserPinecone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_pinecone_id")
    private Long userPineconeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "pinecone_category", nullable = false, length = 20)
    private MissionCategory pineconeCategory;

    @Column(name = "season_key", nullable = false, length = 20)
    private String seasonKey;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public UserPinecone(User user, MissionCategory pineconeCategory, String seasonKey) {
        this.user = user;
        this.pineconeCategory = pineconeCategory;
        this.seasonKey = seasonKey;
        this.earnedAt = LocalDateTime.now();
    }
}