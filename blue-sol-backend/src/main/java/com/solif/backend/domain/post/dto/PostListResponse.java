package com.solif.backend.domain.post.dto;

import com.solif.backend.domain.post.entity.Post;
import com.solif.backend.domain.post.entity.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "게시글 목록 응답")
public class PostListResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "게시판 ID", example = "1")
    private Long boardId;

    @Schema(description = "카테고리", example = "STUDY", nullable = true)
    private PostCategory postCategory;

    @Schema(description = "게시글 제목", example = "부모님과 고등학교 문제로 다투었어요")
    private String postTitle;

    @Schema(description = "게시글 내용 미리보기 (100자)", example = "제주 지역 자치회 구성원들이 처음으로...")
    private String postContentPreview;

    @Schema(description = "썸네일 이미지 URL", example = "https://...")
    private String thumbnailImageUrl;

    @Schema(description = "자치회 이름 (자치회 활동 후기인 경우)", example = "제주 자치회", nullable = true)
    private String councilName;

    @Schema(description = "작성자 이름 (익명일 경우 '익명')", example = "홍길동")
    private String authorName;

    @Schema(description = "조회수", example = "42")
    private Integer viewCount;

    @Schema(description = "댓글 수", example = "10")
    private Long commentCount;

    @Schema(description = "작성 일시", example = "2026-01-28T14:30:00")
    private LocalDateTime createdAt;

    public static PostListResponse from(Post post, Long commentCount, boolean isAnonymous,
                                        String councilName, String thumbnailImageUrl) {
        // 내용 미리보기 (100자)
        String contentPreview = post.getPostContent() != null && post.getPostContent().length() > 100
                ? post.getPostContent().substring(0, 100) + "..."
                : post.getPostContent();

        return PostListResponse.builder()
                .postId(post.getPostId())
                .boardId(post.getBoard().getBoardId())
                .postCategory(post.getPostCategory())
                .postTitle(post.getPostTitle())
                .postContentPreview(contentPreview)
                .thumbnailImageUrl(thumbnailImageUrl)
                .authorName(isAnonymous ? "익명" : post.getAuthor().getName())
                .councilName(councilName)
                .viewCount(post.getViewCount())
                .commentCount(commentCount)
                .createdAt(post.getCreatedAt())
                .build();
    }

    // 기존 호환성을 위한 오버로드 메서드
    public static PostListResponse from(Post post, Long commentCount, boolean isAnonymous) {
        return from(post, commentCount, isAnonymous, null, null);
    }
}