package com.solif.backend.domain.comment.dto;

import com.solif.backend.domain.comment.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "댓글 응답")
public class CommentResponse {

    @Schema(description = "댓글 ID", example = "1")
    private Long commentId;

    @Schema(description = "게시글 ID", example = "10")
    private Long postId;

    @Schema(description = "작성자 이름 (익명일 경우 '익명')", example = "홍길동")
    private String authorName;

    @Schema(description = "작성자 ID (본인 확인용)", example = "5")
    private Long authorId;

    @Schema(description = "댓글 내용", example = "도움이 되는 글이네요!")
    private String commentContent;

    @Schema(description = "익명 여부", example = "false")
    private Boolean commentIsAnonymous;

    @Schema(description = "작성 일시", example = "2026-01-29T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2026-01-29T15:00:00")
    private LocalDateTime updatedAt;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .postId(comment.getPost().getPostId())
                .authorName(comment.getCommentIsAnonymous() ? "익명" : comment.getUser().getName())
                .authorId(comment.getCommentIsAnonymous() ? null : comment.getUser().getUserId())
                .commentContent(comment.getCommentContent())
                .commentIsAnonymous(comment.getCommentIsAnonymous())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}