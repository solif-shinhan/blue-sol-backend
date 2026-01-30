package com.solif.backend.domain.post.dto;

import com.solif.backend.domain.post.entity.Post;
import com.solif.backend.domain.post.entity.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "게시글 상세 응답")
public class PostDetailResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "게시판 ID", example = "1")
    private Long boardId;

    @Schema(description = "카테고리", example = "STUDY", nullable = true)
    private PostCategory postCategory;

    @Schema(description = "게시글 제목", example = "부모님과 고등학교 문제로 다투었어요")
    private String postTitle;

    @Schema(description = "게시글 내용")
    private String postContent;

    @Schema(description = "작성자 ID (익명일 경우 null)", example = "1", nullable = true)
    private Long authorId;

    @Schema(description = "작성자 이름 (익명일 경우 '익명')", example = "홍길동")
    private String authorName;

    @Schema(description = "조회수", example = "43")
    private Integer viewCount;

    @Schema(description = "댓글 수", example = "15")
    private Long commentCount;

    @Schema(description = "좋아요 수", example = "7")
    private Long likeCount;

    @Schema(description = "현재 사용자가 좋아요 했는지 여부", example = "true")
    private Boolean isLikedByUser;

    @Schema(description = "작성 일시", example = "2026-01-28T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2026-01-28T15:00:00", nullable = true)
    private LocalDateTime updatedAt;

    public static PostDetailResponse from(Post post, Long commentCount, Long likeCount, Boolean isLikedByUser, boolean isAnonymous) {
        return PostDetailResponse.builder()
                .postId(post.getPostId())
                .boardId(post.getBoard().getBoardId())
                .postCategory(post.getPostCategory())
                .postTitle(post.getPostTitle())
                .postContent(post.getPostContent())
                .authorId(isAnonymous ? null : post.getAuthor().getUserId())
                .authorName(isAnonymous ? "익명" : post.getAuthor().getName())
                .viewCount(post.getViewCount())
                .commentCount(commentCount)
                .likeCount(likeCount)
                .isLikedByUser(isLikedByUser)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}