package com.solif.backend.domain.council.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "자치회 멤버 삭제 응답")
public class DeleteMemberResponse {

    @Schema(description = "자치회 ID", example = "1")
    private Long councilId;

    @Schema(description = "삭제된 사용자 ID", example = "2")
    private Long deletedUserId;

    @Schema(description = "총 멤버 수", example = "7")
    private Integer totalMemberCount;

    public static DeleteMemberResponse of(Long councilId, Long deletedUserId, int totalMemberCount) {
        return DeleteMemberResponse.builder()
                .councilId(councilId)
                .deletedUserId(deletedUserId)
                .totalMemberCount(totalMemberCount)
                .build();
    }
}