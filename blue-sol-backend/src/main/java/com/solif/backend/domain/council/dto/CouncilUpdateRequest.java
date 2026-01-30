package com.solif.backend.domain.council.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CouncilUpdateRequest {

    @NotBlank(message = "자치회 이름은 필수입니다.")
    private String councilName;

    @NotBlank(message = "활동 지역은 필수입니다.")
    private String region;

    @NotBlank(message = "활동 주제는 필수입니다.")
    private String activityCategory;

    private String description;

    @NotNull(message = "총 예산은 필수입니다.")
    private Long totalBudget;

    private Long profileImageFileId;
}