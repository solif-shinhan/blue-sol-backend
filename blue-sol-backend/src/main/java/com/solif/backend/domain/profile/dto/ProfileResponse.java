package com.solif.backend.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "프로필 조회 응답")
public class ProfileResponse {

    @Schema(description = "프로필 ID")
    private Long profileId;

    @Schema(description = "사용자 이름")
    private String userName;

    @Schema(description = "목표 목록")
    private List<String> mainGoals;

    @Schema(description = "SOLID 목표 이름")
    private String solidGoalName;

    @Schema(description = "캐릭터 식별값")
    private String userCharacter;

    @Schema(description = "캐릭터 실제 이미지 URL")
    private String characterImageUrl;

    @Schema(description = "배경 패턴 식별값")
    private String backgroundPattern;

    @Schema(description = "배경 패턴 실제 이미지 URL") // 추가
    private String backgroundImageUrl;

    @Schema(description = "QR 코드 URL")
    private String qrCodeUrl;

    @Schema(description = "관심사 목록")
    private List<String> interests;
}
