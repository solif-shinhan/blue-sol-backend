package com.solif.backend.domain.goal.controller;

import com.solif.backend.domain.goal.dto.GoalCountResponse;
import com.solif.backend.domain.goal.dto.GoalFirstResponse;
import com.solif.backend.domain.goal.code.GoalSuccessCode;
import com.solif.backend.domain.goal.service.GoalService;
import com.solif.backend.global.common.response.ResponseFactory;
import com.solif.backend.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "홈 화면 목표", description = "나의 목표 API")
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @Operation(
            summary = "첫 번째 목표 조회",
            description = "온보딩에서 작성한 첫 번째 목표를 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/first")
    public ResponseEntity<SuccessResponse<GoalFirstResponse>> getFirstGoal(
            @AuthenticationPrincipal Long userId
    ) {
        GoalFirstResponse response = goalService.getFirstGoal(userId);
        return ResponseFactory.success(GoalSuccessCode.GOAL_FIRST_READ_SUCCESS, response);
    }

    @Operation(
            summary = "목표 개수 조회",
            description = "작성한 목표 개수를 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/count")
    public ResponseEntity<SuccessResponse<GoalCountResponse>> getGoalCount(
            @AuthenticationPrincipal Long userId
    ) {
        GoalCountResponse response = goalService.getGoalCount(userId);
        return ResponseFactory.success(GoalSuccessCode.GOAL_COUNT_READ_SUCCESS, response);
    }
}
