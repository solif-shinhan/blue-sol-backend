package com.solif.backend.domain.councilreview.dto.response;

import com.solif.backend.domain.councilreview.entity.CouncilReviewPost;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "자치회 활동 후기 목록 응답")
public class CouncilReviewPostListResponse {

    @Schema(description = "활동 후기 게시글 ID", example = "1")
    private Long councilReviewPostId;

    @Schema(description = "통합 게시글 ID", example = "100")
    private Long postId;

    @Schema(description = "게시글 제목", example = "우리들의 첫 만남")
    private String postTitle;

    @Schema(description = "활동 날짜", example = "2026-02-10")
    private LocalDate activityDate;

    @Schema(description = "활동 장소", example = "별마당 도서관")
    private String activityLocation;

    @Schema(description = "지출 총액", example = "120380")
    private Long totalCost;

    @Schema(description = "참여 인원 수", example = "6")
    private Long participantCount;

    @Schema(description = "릴레이 개수", example = "3")
    private Long relayCount;

    @Schema(description = "조회수", example = "21")
    private Integer viewCount;

    @Schema(description = "좋아요 수", example = "7")
    private Long likeCount;

    @Schema(description = "댓글 수", example = "10")
    private Long commentCount;

    @Schema(description = "썸네일 이미지 URL (첫 번째 이미지)", example = "https://...")
    private String thumbnailImageUrl;

    @Schema(description = "작성 시간", example = "2026-02-10T15:30:00")
    private LocalDateTime createdAt;

    public static CouncilReviewPostListResponse from(
            CouncilReviewPost post,
            Long participantCount,
            Long relayCount,
            Long likeCount,
            Long commentCount,
            String thumbnailImageUrl
    ) {
        return CouncilReviewPostListResponse.builder()
                .councilReviewPostId(post.getCouncilReviewPostId())
                .postId(post.getPost().getPostId())
                .postTitle(post.getPost().getPostTitle())
                .activityDate(post.getActivityDate())
                .activityLocation(post.getActivityLocation())
                .totalCost(post.getTotalCost())
                .participantCount(participantCount)
                .relayCount(relayCount)
                .viewCount(post.getPost().getViewCount())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .thumbnailImageUrl(thumbnailImageUrl)
                .createdAt(post.getCreatedAt())
                .build();
    }
}