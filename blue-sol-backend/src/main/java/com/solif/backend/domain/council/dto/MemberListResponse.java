package com.solif.backend.domain.council.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "자치회 멤버 목록 응답")
public class MemberListResponse {

    @Schema(description = "자치회 ID", example = "1")
    private Long councilId;

    @Schema(description = "총 멤버 수", example = "5")
    private Integer memberCount;

    @Schema(description = "멤버 목록 (LEADER 먼저, 그 다음 가입일 순)")
    private List<CouncilMemberResponse> members;

    public static MemberListResponse of(Long councilId, List<CouncilMemberResponse> members) {
        return MemberListResponse.builder()
                .councilId(councilId)
                .memberCount(members.size())
                .members(members)
                .build();
    }
}