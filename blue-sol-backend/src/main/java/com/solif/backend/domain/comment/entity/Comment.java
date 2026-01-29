package com.solif.backend.domain.comment.entity;

import com.solif.backend.domain.post.entity.Post;
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
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "comment_content", columnDefinition = "TEXT", nullable = false)
    private String commentContent;

    @Column(name = "comment_is_anonymous", nullable = false)
    private Boolean commentIsAnonymous = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Comment(User user, Post post, String commentContent, Boolean commentIsAnonymous) {
        this.user = user;
        this.post = post;
        this.commentContent = commentContent;
        this.commentIsAnonymous = commentIsAnonymous != null ? commentIsAnonymous : false;
    }

    // 비즈니스 로직
    public void updateContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public boolean isAuthor(Long userId) {
        return this.user.getUserId().equals(userId);
    }
}