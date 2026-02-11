package com.solif.backend.domain.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "미션 팝업 알림 응답 (SSE 전용, DB 저장 안 함)")
public class MissionPopupResponse {

    @Schema(description = "팝업 타입 (CATEGORY_COMPLETE: 카테고리 완료, PINECONE_EARNED: 솔방울 획득)")
    private String popupType;

    @Schema(description = "카테고리명 (연결/성장/기여)")
    private String category;

    @Schema(description = "팝업 메시지")
    private String message;

    @Schema(description = "남은 솔방울 개수 (솔방울 획득 시에만 사용)")
    private Integer remainingCount;
}