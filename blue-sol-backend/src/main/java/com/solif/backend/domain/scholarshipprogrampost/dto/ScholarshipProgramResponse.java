package com.solif.backend.domain.scholarshipprogrampost.dto;

import com.solif.backend.domain.scholarshipprogrampost.entity.ScholarshipProgramPost;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "장학 프로그램 응답")
public class ScholarshipProgramResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "제목", example = "2025.9.11 입직원 직무 멘토링")
    private String title;

    @Schema(description = "내용 미리보기", example = "신한금융그룹 입직원분들과 함께...")
    private String content;

    @Schema(description = "조회수", example = "25")
    private Integer viewCount;

    @Schema(description = "댓글 수", example = "2")
    private Long commentCount;

    @Schema(description = "작성일시", example = "2026-02-19T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "썸네일 URL", example = "https://s3.amazonaws.com/...")
    private String thumbnailUrl;

    public static ScholarshipProgramResponse from(ScholarshipProgramPost scholarshipProgramPost, Long commentCount, String thumbnailUrl) {
        return ScholarshipProgramResponse.builder()
                .postId(scholarshipProgramPost.getPost().getPostId())
                .title(scholarshipProgramPost.getPost().getPostTitle())
                .content(extractPreview(scholarshipProgramPost.getPost().getPostContent()))
                .viewCount(scholarshipProgramPost.getPost().getViewCount())
                .commentCount(commentCount)
                .createdAt(scholarshipProgramPost.getPost().getCreatedAt())
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

    /**
     * 내용 미리보기 추출 (최대 100자)
     */
    private static String extractPreview(String content) {
        if (content == null) {
            return "";
        }
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }
}