package com.solif.backend.domain.council.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "자치회 활동 규칙 추가 응답")
public class AddRuleResponse {

    @Schema(description = "자치회 ID", example = "1")
    private Long councilId;

    @Schema(description = "추가된 규칙 ID", example = "5")
    private Long ruleId;

    @Schema(description = "규칙 내용", example = "공지 올라오면 '체크 이모티콘' 남겨주기")
    private String ruleContent;

    @Schema(description = "총 규칙 수", example = "3")
    private Integer totalRuleCount;

    public static AddRuleResponse of(Long councilId, Long ruleId, String ruleContent, Integer totalRuleCount) {
        return AddRuleResponse.builder()
                .councilId(councilId)
                .ruleId(ruleId)
                .ruleContent(ruleContent)
                .totalRuleCount(totalRuleCount)
                .build();
    }
}