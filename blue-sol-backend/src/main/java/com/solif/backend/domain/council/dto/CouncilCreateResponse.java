package com.solif.backend.domain.council.dto;

import com.solif.backend.domain.council.entity.Council;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CouncilCreateResponse {

    private Long councilId;
    private String councilName;
    private LocalDateTime createdAt;
    private Integer memberCount;

    public static CouncilCreateResponse from(Council council, Integer memberCount) {
        return CouncilCreateResponse.builder()
                .councilId(council.getCouncilId())
                .councilName(council.getCouncilName())
                .createdAt(council.getCreatedAt())
                .memberCount(memberCount)
                .build();
    }
}