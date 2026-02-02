package com.solif.backend.domain.council.controller;

import com.solif.backend.domain.council.code.CouncilSuccessCode;
import com.solif.backend.domain.council.dto.AddRuleRequest;
import com.solif.backend.domain.council.dto.AddRuleResponse;
import com.solif.backend.domain.council.dto.RuleListResponse;
import com.solif.backend.domain.council.service.CouncilRuleService;
import com.solif.backend.global.common.response.ResponseFactory;
import com.solif.backend.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "자치회 활동 규칙", description = "자치회 활동 규칙 관리 API")
@RestController
@RequestMapping("/api/v1/councils/{councilId}/rules")
@RequiredArgsConstructor
public class CouncilRuleController {

    private final CouncilRuleService councilRuleService;

    @Operation(
            summary = "자치회 활동 규칙 목록 조회",
            description = "자치회의 모든 활동 규칙을 조회합니다. (생성일 순)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<SuccessResponse<RuleListResponse>> getRules(
            @Parameter(description = "자치회 ID", required = true, example = "1")
            @PathVariable Long councilId
    ) {
        RuleListResponse response = councilRuleService.getRules(councilId);
        return ResponseFactory.success(CouncilSuccessCode.RULE_LIST_SUCCESS, response);
    }

    @Operation(
            summary = "자치회 활동 규칙 추가",
            description = "자치회에 새로운 활동 규칙을 추가합니다. (LEADER만 가능)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<SuccessResponse<AddRuleResponse>> addRule(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "자치회 ID", required = true, example = "1")
            @PathVariable Long councilId,
            @Valid @RequestBody AddRuleRequest request
    ) {
        AddRuleResponse response = councilRuleService.addRule(userId, councilId, request.getRuleContent());
        return ResponseFactory.success(CouncilSuccessCode.RULE_ADD_SUCCESS, response);
    }

    @Operation(
            summary = "자치회 활동 규칙 삭제",
            description = "자치회에서 활동 규칙을 삭제합니다. (LEADER만 가능)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{ruleId}")
    public ResponseEntity<SuccessResponse<Void>> deleteRule(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "자치회 ID", required = true, example = "1")
            @PathVariable Long councilId,
            @Parameter(description = "삭제할 규칙 ID", required = true, example = "5")
            @PathVariable Long ruleId
    ) {
        councilRuleService.deleteRule(userId, councilId, ruleId);
        return ResponseFactory.success(CouncilSuccessCode.RULE_DELETE_SUCCESS);
    }
}