package com.solif.backend.domain.council.dto;

import com.solif.backend.domain.council.entity.CouncilMember;
import com.solif.backend.domain.council.entity.CouncilMemberRole;
import com.solif.backend.domain.profile.entity.UserProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.function.Function;

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

    @Schema(description = "캐릭터", example = "character1.png")
    private String userCharacter;

    @Schema(description = "캐릭터 이미지 URL", example = "https://bucket.s3.region.amazonaws.com/character1.png")
    private String characterImageUrl;

    @Schema(description = "배경 패턴", example = "pattern1.png")
    private String backgroundPattern;

    @Schema(description = "배경 이미지 URL", example = "https://bucket.s3.region.amazonaws.com/pattern1.png")
    private String backgroundImageUrl;

    public static CouncilMemberResponse from(
            CouncilMember member,
            UserProfile profile,
            Function<String, String> buildS3Url
    ) {
        return CouncilMemberResponse.builder()
                .userId(member.getUser().getUserId())
                .name(member.getUser().getName())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .region(member.getUser().getRegion())
                .schoolName(member.getUser().getSchoolName())
                .userCharacter(profile != null ? profile.getUserCharacter() : null)
                .characterImageUrl(buildS3Url.apply(profile != null ? profile.getUserCharacter() : null))
                .backgroundPattern(profile != null ? profile.getBackgroundPattern() : null)
                .backgroundImageUrl(buildS3Url.apply(profile != null ? profile.getBackgroundPattern() : null))
                .build();
    }
}