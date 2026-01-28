package com.solif.backend.domain.post.entity;

import com.solif.backend.domain.board.entity.Board;
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
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_category", length = 50)
    private PostCategory postCategory;

    @Column(name = "post_title", nullable = false, length = 255)
    private String postTitle;

    @Column(name = "post_content", columnDefinition = "TEXT")
    private String postContent;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Post(User author, Board board, PostCategory postCategory,
                String postTitle, String postContent) {
        this.author = author;
        this.board = board;
        this.postCategory = postCategory;
        this.postTitle = postTitle;
        this.postContent = postContent;
        this.viewCount = 0;
    }

    // 비즈니스 로직
    public void increaseViewCount() {
        this.viewCount++;
    }

    public void updatePost(String postTitle, String postContent) {
        this.postTitle = postTitle;
        this.postContent = postContent;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isAuthor(Long userId) {
        return this.author.getUserId().equals(userId);
    }
}