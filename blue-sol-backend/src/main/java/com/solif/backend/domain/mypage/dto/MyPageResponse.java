package com.solif.backend.domain.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "마이페이지 응답")
public class MyPageResponse {

    @Schema(description = "사용자 이름", example = "김솔잎")
    private String name;

    @Schema(description = "SOLID 목표명", example = "한재대 27학번으로 입학하기")
    private String solidGoalName;

    @Schema(description = "활동 대시보드")
    private Dashboard dashboard;

    @Schema(description = "나의 지난 활동 (자치회 활동 후기 최신순 3개)")
    private List<RecentCouncilReview> recentCouncilReviews;

    @Getter
    @Builder
    @Schema(description = "활동 대시보드")
    public static class Dashboard {

        @Schema(description = "연결 점수 (0-30)", example = "30")
        private Integer connection;

        @Schema(description = "성장 점수 (0-30)", example = "20")
        private Integer growth;

        @Schema(description = "기여 점수 (0-30)", example = "10")
        private Integer contribution;

        @Schema(description = "전체 점수 (0-90)", example = "60")
        private Integer total;

        @Schema(description = "페르소나 타입명", example = "마당발 네트워커")
        private String personaType;
    }

    @Getter
    @Builder
    @Schema(description = "나의 지난 활동 (자치회 활동 후기)")
    public static class RecentCouncilReview {

        @Schema(description = "자치회 활동 후기 게시글 ID", example = "1")
        private Long councilReviewPostId;

        @Schema(description = "통합 게시글 ID", example = "100")
        private Long postId;

        @Schema(description = "게시글 제목", example = "우리들의 첫 만남")
        private String title;

        @Schema(description = "활동 날짜", example = "2026-02-10")
        private LocalDate activityDate;

        @Schema(description = "작성 시간", example = "2026-02-10T15:30:00")
        private LocalDateTime createdAt;
    }

    public static MyPageResponse of(
            String name,
            String solidGoalName,
            Dashboard dashboard,
            List<RecentCouncilReview> recentCouncilReviews
    ) {
        return MyPageResponse.builder()
                .name(name)
                .solidGoalName(solidGoalName)
                .dashboard(dashboard)
                .recentCouncilReviews(recentCouncilReviews)
                .build();
    }
}