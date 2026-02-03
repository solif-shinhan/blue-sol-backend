package com.solif.backend.domain.councilreview.dto.response;

import com.solif.backend.domain.councilreview.entity.CouncilReviewPost;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "자치회 활동 후기 상세 응답")
public class CouncilReviewPostDetailResponse {

    @Schema(description = "활동 후기 게시글 ID", example = "1")
    private Long councilReviewPostId;

    @Schema(description = "통합 게시글 ID", example = "100")
    private Long postId;

    @Schema(description = "자치회 ID", example = "10")
    private Long councilId;

    @Schema(description = "자치회 이름", example = "재주자치회")
    private String councilName;

    @Schema(description = "게시글 제목", example = "우리들의 첫 만남")
    private String postTitle;

    @Schema(description = "활동 날짜", example = "2026-02-10")
    private LocalDate activityDate;

    @Schema(description = "활동 장소", example = "별마당 도서관")
    private String activityLocation;

    @Schema(description = "지출 총액", example = "120380")
    private Long totalCost;

    @Schema(description = "활동 이미지 URL 목록")
    private List<String> imageUrls;

    @Schema(description = "참여자 목록")
    private List<CouncilReviewParticipantResponse> participants;

    @Schema(description = "릴레이 목록")
    private List<CouncilReviewRelayResponse> relays;

    @Schema(description = "조회수", example = "21")
    private Integer viewCount;

    @Schema(description = "좋아요 수", example = "7")
    private Long likeCount;

    @Schema(description = "댓글 수", example = "10")
    private Long commentCount;

    @Schema(description = "내가 좋아요 눌렀는지 여부", example = "false")
    private Boolean isLikedByMe;

    @Schema(description = "내가 리더인지 여부", example = "true")
    private Boolean isLeader;

    @Schema(description = "작성 시간", example = "2026-02-10T15:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "null")
    private LocalDateTime updatedAt;

    public static CouncilReviewPostDetailResponse of(
            CouncilReviewPost post,
            List<String> imageUrls,
            List<CouncilReviewParticipantResponse> participants,
            List<CouncilReviewRelayResponse> relays,
            Long likeCount,
            Long commentCount,
            Boolean isLikedByMe,
            Boolean isLeader
    ) {
        return CouncilReviewPostDetailResponse.builder()
                .councilReviewPostId(post.getCouncilReviewPostId())
                .postId(post.getPost().getPostId())
                .councilId(post.getCouncil().getCouncilId())
                .councilName(post.getCouncil().getCouncilName())
                .postTitle(post.getPost().getPostTitle())
                .activityDate(post.getActivityDate())
                .activityLocation(post.getActivityLocation())
                .totalCost(post.getTotalCost())
                .imageUrls(imageUrls)
                .participants(participants)
                .relays(relays)
                .viewCount(post.getPost().getViewCount())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .isLikedByMe(isLikedByMe)
                .isLeader(isLeader)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}