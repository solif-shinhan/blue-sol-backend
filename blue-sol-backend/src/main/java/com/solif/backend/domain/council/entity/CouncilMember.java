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
@Table(name = "council_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CouncilMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "council_member_id")
    private Long councilMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "council_id", nullable = false)
    private Council council;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private CouncilMemberRole role = CouncilMemberRole.MEMBER;

    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Builder
    public CouncilMember(User user, Council council, CouncilMemberRole role) {
        this.user = user;
        this.council = council;
        this.role = role != null ? role : CouncilMemberRole.MEMBER;
    }

    // 비즈니스 로직
    public boolean isLeader() {
        return this.role == CouncilMemberRole.LEADER;
    }

    public boolean isMember() {
        return this.role == CouncilMemberRole.MEMBER;
    }
}