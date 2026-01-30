package com.solif.backend.domain.council.dto;

import com.solif.backend.domain.council.entity.Council;
import com.solif.backend.domain.council.entity.CouncilMemberRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CouncilMyResponse {

    private Long councilId;
    private String councilName;
    private Long currentBudget;
    private Long totalBudget;
    private Long memberCount;
    private String role;

    public static CouncilMyResponse from(
            Council council,
            Long memberCount,
            CouncilMemberRole role
    ) {
        return CouncilMyResponse.builder()
                .councilId(council.getCouncilId())
                .councilName(council.getCouncilName())
                .currentBudget(council.getCurrentBudget())
                .totalBudget(council.getTotalBudget())
                .memberCount(memberCount)
                .role(role.name())
                .build();
    }
}