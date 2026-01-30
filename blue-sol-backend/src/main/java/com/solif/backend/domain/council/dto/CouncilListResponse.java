package com.solif.backend.domain.council.dto;

import com.solif.backend.domain.council.entity.Council;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CouncilListResponse {

    private Long councilId;
    private String councilName;
    private Long memberCount;
    private String profileImageUrl;

    public static CouncilListResponse from(Council council, Long memberCount) {
        return CouncilListResponse.builder()
                .councilId(council.getCouncilId())
                .councilName(council.getCouncilName())
                .memberCount(memberCount)
                .profileImageUrl(null)  // 이미지 처리
                .build();
    }
}