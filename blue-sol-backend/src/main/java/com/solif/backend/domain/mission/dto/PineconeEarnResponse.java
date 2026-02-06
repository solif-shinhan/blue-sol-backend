package com.solif.backend.domain.mission.dto;

import com.solif.backend.domain.mission.entity.MissionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "솔방울 획득 응답")
public class PineconeEarnResponse {

    @Schema(description = "솔방울 ID")
    private Long pineconeId;

    @Schema(description = "카테고리 (CONNECT/GROW/IMPACT)")
    private MissionCategory category;

    @Schema(description = "카테고리명 (연결/성장/기여)")
    private String categoryName;

    @Schema(description = "시즌 (예: 2026-H1)")
    private String seasonKey;

    @Schema(description = "획득 시각")
    private LocalDateTime earnedAt;
}