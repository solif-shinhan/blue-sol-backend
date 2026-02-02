package com.solif.backend.domain.council.controller;

import com.solif.backend.domain.council.code.CouncilSuccessCode;
import com.solif.backend.domain.council.dto.AddMemberRequest;
import com.solif.backend.domain.council.dto.AddMemberResponse;
import com.solif.backend.domain.council.dto.MemberListResponse;
import com.solif.backend.domain.council.service.CouncilMemberService;
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

@Tag(name = "자치회 멤버", description = "자치회 멤버 관리 API")
@RestController
@RequestMapping("/api/v1/councils/{councilId}/members")
@RequiredArgsConstructor
public class CouncilMemberController {

    private final CouncilMemberService councilMemberService;

    @Operation(
            summary = "자치회 멤버 목록 조회",
            description = "자치회의 모든 멤버를 조회합니다. (LEADER 먼저, 가입일 순)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<SuccessResponse<MemberListResponse>> getMembers(
            @Parameter(description = "자치회 ID", required = true, example = "1")
            @PathVariable Long councilId
    ) {
        MemberListResponse response = councilMemberService.getMembers(councilId);
        return ResponseFactory.success(CouncilSuccessCode.MEMBER_LIST_SUCCESS, response);
    }

    @Operation(
            summary = "자치회 멤버 추가",
            description = "자치회에 새로운 멤버를 추가합니다. (LEADER만 가능)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<SuccessResponse<AddMemberResponse>> addMembers(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "자치회 ID", required = true, example = "1")
            @PathVariable Long councilId,
            @Valid @RequestBody AddMemberRequest request
    ) {
        AddMemberResponse response = councilMemberService.addMembers(userId, councilId, request.getUserIds());
        return ResponseFactory.success(CouncilSuccessCode.MEMBER_ADD_SUCCESS, response);
    }

    @Operation(
            summary = "자치회 멤버 삭제",
            description = "자치회에서 멤버를 삭제합니다. (LEADER만 가능, 본인 삭제 불가)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{userId}")
    public ResponseEntity<SuccessResponse<Void>> deleteMember(
            @AuthenticationPrincipal Long currentUserId,
            @Parameter(description = "자치회 ID", required = true, example = "1")
            @PathVariable Long councilId,
            @Parameter(description = "삭제할 사용자 ID", required = true, example = "2")
            @PathVariable("userId") Long userIdToDelete
    ) {
        councilMemberService.deleteMember(currentUserId, councilId, userIdToDelete);
        return ResponseFactory.success(CouncilSuccessCode.MEMBER_DELETE_SUCCESS);
    }
}