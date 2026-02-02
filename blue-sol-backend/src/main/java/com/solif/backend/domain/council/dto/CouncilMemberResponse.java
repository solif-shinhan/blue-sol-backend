package com.solif.backend.domain.council.dto;

import com.solif.backend.domain.council.entity.CouncilMember;
import com.solif.backend.domain.council.entity.CouncilMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "자치회 멤버 정보")
public class CouncilMemberResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자 이름", example = "김지환")
    private String name;

    @Schema(description = "역할", example = "LEADER")
    private CouncilMemberRole role;

    @Schema(description = "가입 일시", example = "2025-03-01T10:00:00")
    private LocalDateTime joinedAt;

    @Schema(description = "지역", example = "제주")
    private String region;

    @Schema(description = "학교명", example = "제주대학교")
    private String schoolName;

    public static CouncilMemberResponse from(CouncilMember member) {
        return CouncilMemberResponse.builder()
                .userId(member.getUser().getUserId())
                .name(member.getUser().getName())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .region(member.getUser().getRegion())
                .schoolName(member.getUser().getSchoolName())
                .build();
    }
}