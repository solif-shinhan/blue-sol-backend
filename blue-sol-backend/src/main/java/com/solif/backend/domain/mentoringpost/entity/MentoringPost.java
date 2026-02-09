package com.solif.backend.domain.mentoringpost.entity;

import com.solif.backend.domain.post.entity.Post;
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
@Table(name = "mentoring_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MentoringPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mentoring_post_id")
    private Long mentoringPostId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public MentoringPost(Post post) {
        this.post = post;
    }

    // 비즈니스 로직
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.post.softDelete();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isAuthor(Long userId) {
        return this.post.isAuthor(userId);
    }
}