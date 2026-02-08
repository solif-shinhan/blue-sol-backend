package com.solif.backend.domain.mission.dto;

import com.solif.backend.domain.mission.entity.MissionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "솔방울 추억 회상 응답")
public class PineconeMemoryResponse {

    @Schema(description = "카테고리 (CONNECT/GROW/IMPACT)")
    private MissionCategory category;

    @Schema(description = "카테고리명 (연결/성장/기여)")
    private String categoryName;

    @Schema(description = "완료한 미션 3개")
    private List<CompletedMission> completedMissions;

    @Getter
    @Builder
    @Schema(description = "완료한 미션 상세")
    public static class CompletedMission {
        @Schema(description = "미션 제목")
        private String missionTitle;

        @Schema(description = "미션 완료 여부")
        private Boolean isCompleted;

        @Schema(description = "추억 상세 조회 가능 여부")
        private Boolean hasMemoryDetail;

        @Schema(description = "추억 상세 (있을 경우)")
        private MemoryDetail memoryDetail;
    }

    @Getter
    @Builder
    @Schema(description = "추억 상세")
    public static class MemoryDetail {
        @Schema(description = "추억 타입 (MESSAGE/POST/COMMENT/MENTORING)")
        private String memoryType;

        @Schema(description = "추억 제목/내용")
        private String memoryContent;

        @Schema(description = "추억 관련 사용자명 (있을 경우)")
        private String relatedUserName;

        @Schema(description = "추억 생성 시각")
        private String createdAt;
    }
}