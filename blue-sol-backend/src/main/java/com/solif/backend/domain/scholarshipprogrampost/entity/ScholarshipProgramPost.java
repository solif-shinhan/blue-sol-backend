package com.solif.backend.domain.scholarshipprogrampost.entity;

import com.solif.backend.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scholarship_program_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScholarshipProgramPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scholarship_program_post_id")
    private Long scholarshipProgramPostId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Builder
    public ScholarshipProgramPost(Post post) {
        this.post = post;
    }

    // 비즈니스 로직
    public boolean isDeleted() {
        return this.post.isDeleted();
    }

    public boolean isAuthor(Long userId) {
        return this.post.isAuthor(userId);
    }
}