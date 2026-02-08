package com.solif.backend.domain.network.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "교류망 추천 조회 응답")
public class NetworkRecommendationResponse {

    @Schema(description = "관심사 기반 추천")
    private RecommendationGroup interestBased;

    @Schema(description = "전체 사용자 둘러보기")
    private RecommendationGroup allUsers;

    @Getter
    @Builder
    @Schema(description = "추천 그룹")
    public static class RecommendationGroup {
        @Schema(description = "그룹 타이틀")
        private String title;

        @Schema(description = "추천 사용자 목록")
        private List<RecommendedUser> users;
    }

    @Getter
    @Builder
    @Schema(description = "추천 사용자 정보")
    public static class RecommendedUser {
        @Schema(description = "사용자 ID")
        private Long userId;

        @Schema(description = "사용자 이름")
        private String userName;

        @Schema(description = "캐릭터")
        private String userCharacter;

        @Schema(description = "캐릭터 이미지 URL")
        private String characterImageUrl;

        @Schema(description = "배경 패턴")
        private String backgroundPattern;

        @Schema(description = "배경 이미지 URL")
        private String backgroundImageUrl;

        @Schema(description = "SOLID 목표 이름")
        private String solidGoalName;

        @Schema(description = "관심사 목록")
        private List<String> interests;

        @Schema(description = "자치회 소속 여부", example = "true")
        private Boolean isInCouncil;

        @Schema(description = "소속 자치회 이름", example = "제주최강산한이들")
        private String councilName;

        @Schema(description = "학교 이름 (장학생) 또는 직업 (졸업생)")
        private String schoolName;

        @Schema(description = "가입 연도", example = "2026")
        private Integer joinYear;
    }
}
