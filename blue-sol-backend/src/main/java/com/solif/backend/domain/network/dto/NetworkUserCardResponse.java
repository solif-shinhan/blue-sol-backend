package com.solif.backend.domain.network.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "교류망 사용자 카드 조회 응답")
public class NetworkUserCardResponse {

    @Schema(description = "사용자 ID")
    private Long userId;

    @Schema(description = "사용자 이름")
    private String userName;

    @Schema(description = "캐릭터")
    private String userCharacter;

    @Schema(description = "캐릭터 이미지 URL")
    private String characterImageUrl;

    @Schema(description = "배경 패턴")
    private String backgroundPattern;

    @Schema(description = "배경 이미지 URL")
    private String backgroundImageUrl;

    @Schema(description = "SOLID 목표 이름")
    private String solidGoalName;

    @Schema(description = "관심사 목록")
    private List<String> interests;

    @Schema(description = "가입 연도", example = "2026")
    private Integer joinYear;
}