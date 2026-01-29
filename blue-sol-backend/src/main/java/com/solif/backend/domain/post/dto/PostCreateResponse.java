package com.solif.backend.domain.post.dto;

import com.solif.backend.domain.post.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "게시글 작성 응답")
public class PostCreateResponse {

    @Schema(description = "생성된 게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "게시글 제목", example = "부모님과 고등학교 문제로 다투었어요")
    private String postTitle;

    @Schema(description = "작성 일시", example = "2026-01-28T14:30:00")
    private LocalDateTime createdAt;

    public static PostCreateResponse from(Post post) {
        return PostCreateResponse.builder()
                .postId(post.getPostId())
                .postTitle(post.getPostTitle())
                .createdAt(post.getCreatedAt())
                .build();
    }
}