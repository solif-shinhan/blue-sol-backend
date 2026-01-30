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

    private String description;

    @NotNull(message = "총 예산은 필수입니다.")
    private Long totalBudget;
}