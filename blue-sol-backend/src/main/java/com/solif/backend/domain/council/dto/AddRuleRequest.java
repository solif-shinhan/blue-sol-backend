package com.solif.backend.domain.council.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "자치회 활동 규칙 추가 요청")
public class AddRuleRequest {

    @NotBlank(message = "규칙 내용은 필수입니다.")
    @Size(max = 255, message = "규칙 내용은 최대 255자까지 입력 가능합니다.")
    @Schema(description = "규칙 내용", example = "공지 올라오면 '체크 이모티콘' 남겨주기")
    private String ruleContent;
}