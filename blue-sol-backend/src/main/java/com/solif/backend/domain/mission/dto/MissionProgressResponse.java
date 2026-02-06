package com.solif.backend.domain.mission.dto;

import com.solif.backend.domain.mission.entity.MissionCategory;
import com.solif.backend.domain.mission.entity.MissionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "성장 탭 - 미션 진행 상황 응답")
public class MissionProgressResponse {

    @Schema(description = "현재 시즌 (예: 2026-H1)")
    private String currentSeason;

    @Schema(description = "획득한 솔방울 개수 (0~3)")
    private Integer earnedPineconeCount;

    @Schema(description = "D-Day (상반기 종료까지 남은 일수)")
    private Integer daysUntilSeasonEnd;

    @Schema(description = "상반기 미션 리스트 (카테고리별 솔방울 획득 상태)")
    private List<CategoryProgress> categoryProgress;

    @Schema(description = "이번주 미션 리스트 (최대 3개)")
    private List<WeeklyMissionCard> weeklyMissions;

    @Getter
    @Builder
    @Schema(description = "카테고리별 솔방울 획득 상태")
    public static class CategoryProgress {
        @Schema(description = "카테고리 (CONNECT/GROW/IMPACT)")
        private MissionCategory category;

        @Schema(description = "카테고리명 (연결/성장/기여)")
        private String categoryName;

        @Schema(description = "완료된 미션 수 (0~3)")
        private Integer completedCount;

        @Schema(description = "전체 미션 수 (3)")
        private Integer totalCount;

        @Schema(description = "솔방울 획득 여부")
        private Boolean isPineconeEarned;

        @Schema(description = "솔방울 수령 가능 여부 (3개 모두 완료 && 아직 미수령)")
        private Boolean canClaimPinecone;
    }

    @Getter
    @Builder
    @Schema(description = "이번주 미션 카드")
    public static class WeeklyMissionCard {
        @Schema(description = "미션 ID")
        private Long missionId;

        @Schema(description = "카테고리 (CONNECT/GROW/IMPACT)")
        private MissionCategory category;

        @Schema(description = "카테고리명 (연결/성장/기여)")
        private String categoryName;

        @Schema(description = "미션 제목")
        private String missionTitle;

        @Schema(description = "미션 설명")
        private String description;

        @Schema(description = "진행도 (예: 2/5)")
        private String progress;

        @Schema(description = "현재 진행 수")
        private Integer currentCount;

        @Schema(description = "목표 수")
        private Integer targetCount;

        @Schema(description = "미션 상태 (IN_PROGRESS/COMPLETED)")
        private MissionStatus status;

        @Schema(description = "완료 여부")
        private Boolean isCompleted;
    }
}