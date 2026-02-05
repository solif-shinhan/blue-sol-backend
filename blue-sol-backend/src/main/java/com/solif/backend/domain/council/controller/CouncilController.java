package com.solif.backend.domain.council.controller;

import com.solif.backend.domain.council.code.CouncilSuccessCode;
import com.solif.backend.domain.council.dto.*;
import com.solif.backend.domain.council.service.CouncilService;
import com.solif.backend.global.common.response.ResponseFactory;
import com.solif.backend.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "자치회", description = "자치회 API")
@RestController
@RequestMapping("/api/v1/councils")
@RequiredArgsConstructor
public class CouncilController {

    private final CouncilService councilService;

    @Operation(
            summary = "자치회 목록 조회",
            description = "전체 자치회 목록을 조회합니다. 본인 자치회 정보도 포함됩니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping
    public ResponseEntity<SuccessResponse<CouncilListResponse>> getCouncils(
            @AuthenticationPrincipal Long userId
    ) {
        CouncilListResponse response = councilService.getCouncils(userId);
        return ResponseFactory.success(CouncilSuccessCode.COUNCIL_LIST_SUCCESS, response);
    }

    @Operation(
            summary = "내 자치회 조회",
            description = "현재 로그인한 사용자의 소속 자치회 정보를 조회합니다. (홈 화면 위젯용)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/my")
    public ResponseEntity<SuccessResponse<CouncilMyResponse>> getMyCouncil(
            @AuthenticationPrincipal Long userId
    ) {
        CouncilMyResponse myCouncil = councilService.getMyCouncil(userId);
        return ResponseFactory.success(CouncilSuccessCode.MY_COUNCIL_SUCCESS, myCouncil);
    }

    @Operation(
            summary = "자치회 상세 조회",
            description = "자치회 상세 정보를 조회합니다. 본인 자치회와 다른 자치회의 응답이 다릅니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/{councilId}")
    public ResponseEntity<SuccessResponse<CouncilDetailResponse>> getCouncilDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long councilId
    ) {
        CouncilDetailResponse council = councilService.getCouncilDetail(userId, councilId);
        return ResponseFactory.success(CouncilSuccessCode.COUNCIL_DETAIL_SUCCESS, council);
    }

    @Operation(
            summary = "자치회 생성",
            description = "새로운 자치회를 생성합니다. 생성자는 자동으로 리더로 등록됩니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping
    public ResponseEntity<SuccessResponse<CouncilCreateResponse>> createCouncil(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CouncilCreateRequest request
    ) {
        CouncilCreateResponse response = councilService.createCouncil(userId, request);
        return ResponseFactory.success(CouncilSuccessCode.COUNCIL_CREATE_SUCCESS, response);
    }

    @Operation(
            summary = "자치회 수정",
            description = "자치회 정보를 수정합니다. 리더만 수정 가능합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PatchMapping("/{councilId}")
    public ResponseEntity<SuccessResponse<Void>> updateCouncil(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long councilId,
            @Valid @RequestBody CouncilUpdateRequest request
    ) {
        councilService.updateCouncil(userId, councilId, request);
        return ResponseFactory.success(CouncilSuccessCode.COUNCIL_UPDATE_SUCCESS);
    }
}