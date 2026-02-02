package com.solif.backend.domain.council.dto;

import com.solif.backend.domain.council.entity.CouncilRule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "자치회 활동 규칙 정보")
public class RuleResponse {

    @Schema(description = "규칙 ID", example = "1")
    private Long ruleId;

    @Schema(description = "규칙 내용", example = "공지 올라오면 '네크 이더라도' 남겨주기")
    private String ruleContent;

    @Schema(description = "생성 일시", example = "2025-03-01T10:00:00")
    private LocalDateTime createdAt;

    public static RuleResponse from(CouncilRule rule) {
        return RuleResponse.builder()
                .ruleId(rule.getCouncilRuleId())
                .ruleContent(rule.getRuleContent())
                .createdAt(rule.getCreatedAt())
                .build();
    }
}