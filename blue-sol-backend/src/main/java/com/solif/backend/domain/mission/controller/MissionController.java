package com.solif.backend.domain.mission.controller;

import com.solif.backend.domain.mission.code.MissionSuccessCode;
import com.solif.backend.domain.mission.dto.MissionProgressResponse;
import com.solif.backend.domain.mission.dto.PineconeEarnResponse;
import com.solif.backend.domain.mission.dto.PineconeMemoryResponse;
import com.solif.backend.domain.mission.entity.MissionCategory;
import com.solif.backend.domain.mission.service.MissionService;
import com.solif.backend.global.common.response.ResponseFactory;
import com.solif.backend.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "미션 API", description = "성장 페이지 - 미션 시스템 관련 API")
@RestController
@RequestMapping("/api/v1/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @Operation(
            summary = "미션 진행 상황 조회",
            description = "성장 탭에서 현재 미션 진행 상황을 조회합니다. (솔방울 개수, D-Day, 카테고리별 진행도, 이번주 미션)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/progress")
    public ResponseEntity<SuccessResponse<MissionProgressResponse>> getMissionProgress(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        MissionProgressResponse response = missionService.getMissionProgress(userId);
        return ResponseFactory.success(MissionSuccessCode.MISSION_PROGRESS_SUCCESS, response);
    }

    @Operation(
            summary = "솔방울 획득 (받기 버튼)",
            description = "카테고리별 미션 3개를 모두 완료한 후 솔방울을 획득합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/pinecones/{category}")
    public ResponseEntity<SuccessResponse<PineconeEarnResponse>> earnPinecone(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "카테고리 (CONNECT/GROW/IMPACT)", required = true)
            @PathVariable MissionCategory category
    ) {
        PineconeEarnResponse response = missionService.earnPinecone(userId, category);
        return ResponseFactory.success(MissionSuccessCode.PINECONE_EARNED_SUCCESS, response);
    }

    @Operation(
            summary = "솔방울 추억 회상",
            description = "획득한 솔방울을 클릭하여 완료한 미션 3개의 추억을 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/pinecones/{category}/memories")
    public ResponseEntity<SuccessResponse<PineconeMemoryResponse>> getPineconeMemory(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "카테고리 (CONNECT/GROW/IMPACT)", required = true)
            @PathVariable MissionCategory category
    ) {
        PineconeMemoryResponse response = missionService.getPineconeMemory(userId, category);
        return ResponseFactory.success(MissionSuccessCode.PINECONE_MEMORY_SUCCESS, response);
    }
}