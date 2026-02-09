package com.solif.backend.domain.mentoring.entity;

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
@Table(name = "mentoring_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MentoringRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mentoring_request_id")
    private Long mentoringRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_user_id", nullable = false)
    private User menteeUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_post_id")
    private Post reviewPost;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private MentoringCategory category;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 50)
    private MentoringMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private MentoringRequestStatus status = MentoringRequestStatus.PENDING;

    // 관리자 답변
    @Column(name = "admin_reply", columnDefinition = "TEXT")
    private String adminReply;

    // 답변 작성 일시
    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public MentoringRequest(Mentor mentor, User menteeUser, MentoringCategory category,
                            String content, MentoringMethod method) {
        this.mentor = mentor;
        this.menteeUser = menteeUser;
        this.category = category;
        this.content = content;
        this.method = method;
        this.status = MentoringRequestStatus.PENDING;
    }

    // 비즈니스 로직
    public void updateStatus(MentoringRequestStatus newStatus) {
        this.status = newStatus;
    }

    public void linkReviewPost(Post reviewPost) {
        this.reviewPost = reviewPost;
    }

    // 관리자 답변 작성
    public void replyByAdmin(String adminReply) {
        this.adminReply = adminReply;
        this.repliedAt = LocalDateTime.now();
        // 답변 작성 시 상태를 APPROVED 또는 REJECTED로 변경 가능
        // (상태 변경은 관리자가 선택)
    }

    public boolean isMentee(Long userId) {
        return this.menteeUser.getUserId().equals(userId);
    }

    // 답변 여부 확인
    public boolean hasReply() {
        return this.adminReply != null && !this.adminReply.isEmpty();
    }
}