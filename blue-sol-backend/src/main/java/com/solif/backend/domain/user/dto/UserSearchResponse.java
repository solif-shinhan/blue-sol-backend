package com.solif.backend.domain.user.dto;

import com.solif.backend.domain.council.entity.CouncilMember;
import com.solif.backend.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "사용자 검색 응답")
public class UserSearchResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자 이름", example = "김지환")
    private String name;

    @Schema(description = "지역", example = "제주")
    private String region;

    @Schema(description = "학교명", example = "제주대학교")
    private String schoolName;

    @Schema(description = "자치회 소속 여부", example = "true")
    private Boolean isInCouncil;

    @Schema(description = "소속 자치회 이름", example = "제주최강산한이들")
    private String councilName;

    public static UserSearchResponse from(User user, CouncilMember membership) {
        return UserSearchResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .region(user.getRegion())
                .schoolName(user.getSchoolName())
                .isInCouncil(membership != null)
                .councilName(membership != null ? membership.getCouncil().getCouncilName() : null)
                .build();
    }
}