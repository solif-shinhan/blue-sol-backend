package com.solif.backend.domain.mentoring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "멘토링 홈 응답")
public class MentoringHomeResponse {

    @Schema(description = "전문가 멘토 목록 (최대 4개)")
    private List<MentorListResponse> mentors;

    @Schema(description = "선후배 멘토링 사용자 목록 (응원하기)")
    private SeniorJuniorMentoringResponse cheerList;

    @Schema(description = "선후배 멘토링 사용자 목록 (경험나누기)")
    private SeniorJuniorMentoringResponse helpList;

    @Schema(description = "멘토링 후기 목록 (최대 4개)")
    private List<MentoringReviewResponse> reviews;

    @Getter
    @Builder
    @Schema(description = "선후배 멘토링 응답")
    public static class SeniorJuniorMentoringResponse {

        @Schema(description = "사용자 목록")
        private List<UserCardResponse> users;

        @Getter
        @Builder
        @Schema(description = "사용자 카드 응답")
        public static class UserCardResponse {
            @Schema(description = "사용자 ID", example = "1")
            private Long userId;

            @Schema(description = "사용자 이름", example = "김철수")
            private String userName;

            @Schema(description = "캐릭터", example = "character_1")
            private String character;

            @Schema(description = "배경 패턴", example = "pattern_1")
            private String backgroundPattern;

            @Schema(description = "단단한 목표 이름", example = "취업 준비")
            private String solidGoalName;

            @Schema(description = "관심사 목록")
            private List<String> interests;

            @Schema(description = "상태 (PENDING: 대기중, COMPLETED: 양방향 완료)", example = "PENDING")
            private String status;
        }
    }

    @Getter
    @Builder
    @Schema(description = "멘토링 후기 응답")
    public static class MentoringReviewResponse {
        @Schema(description = "게시글 ID", example = "1")
        private Long postId;

        @Schema(description = "제목", example = "신한 멘토님과의 멘토링 후기")
        private String title;

        @Schema(description = "작성자", example = "김철수")
        private String authorName;

        @Schema(description = "카테고리", example = "STUDY")
        private String category;

        @Schema(description = "조회수", example = "150")
        private Integer viewCount;
    }
}