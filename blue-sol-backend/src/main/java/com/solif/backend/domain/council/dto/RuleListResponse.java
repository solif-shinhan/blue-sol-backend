package com.solif.backend.domain.council.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "자치회 활동 규칙 목록 응답")
public class RuleListResponse {

    @Schema(description = "자치회 ID", example = "1")
    private Long councilId;

    @Schema(description = "총 규칙 수", example = "3")
    private Integer ruleCount;

    @Schema(description = "규칙 목록 (생성일 순)")
    private List<RuleResponse> rules;

    public static RuleListResponse of(Long councilId, List<RuleResponse> rules) {
        return RuleListResponse.builder()
                .councilId(councilId)
                .ruleCount(rules.size())
                .rules(rules)
                .build();
    }
}