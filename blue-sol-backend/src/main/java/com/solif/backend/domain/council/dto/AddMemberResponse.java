package com.solif.backend.domain.council.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "자치회 멤버 추가 응답")
public class AddMemberResponse {

    @Schema(description = "자치회 ID", example = "1")
    private Long councilId;

    @Schema(description = "추가된 멤버 수", example = "3")
    private Integer addedCount;

    @Schema(description = "총 멤버 수", example = "8")
    private Integer totalMemberCount;

    public static AddMemberResponse of(Long councilId, int addedCount, int totalMemberCount) {
        return AddMemberResponse.builder()
                .councilId(councilId)
                .addedCount(addedCount)
                .totalMemberCount(totalMemberCount)
                .build();
    }
}