package com.solif.backend.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "프로필 생성 요청")
public class ProfileCreateRequest {

    @NotEmpty(message = "목표를 입력해주세요.")
    @Size(max = 5, message = "목표는 최대 5개까지 입력 가능합니다.")
    @Schema(description = "이루고 싶은 목표 (최대 5개)", example = "[\"한체대 입학하기\", \"토익 900점\"]")
    private List<String> mainGoals;

    @NotBlank(message = "SOLID 목표 이름을 입력해주세요.")
    @Schema(description = "SOLID 목표 이름", example = "체육교사꿈나무")
    private String solidGoalName;

    @NotBlank(message = "캐릭터를 선택해주세요.")
    @Schema(description = "선택한 캐릭터 식별값", example = "char_01")
    private String userCharacter;

    @NotBlank(message = "배경 패턴을 선택해주세요.")
    @Schema(description = "선택한 배경 패턴", example = "bg_01")
    private String backgroundPattern;
}
