package com.solif.backend.domain.councilreview.controller;

import com.solif.backend.domain.councilreview.code.CouncilReviewSuccessCode;
import com.solif.backend.domain.councilreview.dto.request.CouncilReviewRelayCreateRequest;
import com.solif.backend.domain.councilreview.dto.request.CouncilReviewRelayUpdateRequest;
import com.solif.backend.domain.councilreview.dto.response.CouncilReviewQuestionResponse;
import com.solif.backend.domain.councilreview.service.CouncilReviewQuestionService;
import com.solif.backend.domain.councilreview.service.CouncilReviewRelayService;
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

import java.util.List;
import java.util.Map;

@Tag(name = "자치회 활동 후기 릴레이", description = "자치회 활동 후기 릴레이 및 질문 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CouncilReviewRelayController {

    private final CouncilReviewRelayService relayService;
    private final CouncilReviewQuestionService questionService;

    @Operation(
            summary = "릴레이 작성",
            description = "자치회 멤버가 활동 후기에 릴레이 글을 이어쓰기합니다. 참여자만 작성 가능하며 1인 1회 제한입니다. 질문은 중복 사용할 수 없습니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/council-review-posts/{postId}/relays")
    public ResponseEntity<SuccessResponse<Map<String, Object>>> createRelay(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody CouncilReviewRelayCreateRequest request
    ) {
        Map<String, Object> response = relayService.createRelay(userId, postId, request);
        return ResponseFactory.success(CouncilReviewSuccessCode.COUNCIL_REVIEW_RELAY_CREATE_SUCCESS, response);
    }

    @Operation(
            summary = "릴레이 수정",
            description = "본인이 작성한 릴레이 글을 수정합니다. 작성자만 수정 가능하며 질문은 수정할 수 없습니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PatchMapping("/council-review-relays/{relayId}")
    public ResponseEntity<SuccessResponse<Void>> updateRelay(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long relayId,
            @Valid @RequestBody CouncilReviewRelayUpdateRequest request
    ) {
        relayService.updateRelay(userId, relayId, request);
        return ResponseFactory.success(CouncilReviewSuccessCode.COUNCIL_REVIEW_RELAY_UPDATE_SUCCESS);
    }

    @Operation(
            summary = "릴레이 삭제",
            description = "본인이 작성한 릴레이 글을 삭제합니다 (Hard Delete). 작성자만 삭제 가능하며 삭제 후 relay_order가 자동으로 재정렬됩니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/council-review-relays/{relayId}")
    public ResponseEntity<SuccessResponse<Void>> deleteRelay(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long relayId
    ) {
        relayService.deleteRelay(userId, relayId);
        return ResponseFactory.success(CouncilReviewSuccessCode.COUNCIL_REVIEW_RELAY_DELETE_SUCCESS);
    }

    @Operation(
            summary = "랜덤 질문 조회",
            description = "활성화된 질문 중 랜덤으로 1개를 조회합니다. 이미 사용된 질문 ID 목록을 제외할 수 있습니다. 작성 시 질문 새로고침 기능에 사용됩니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/council-review-questions/random")
    public ResponseEntity<SuccessResponse<CouncilReviewQuestionResponse>> getRandomQuestion(
            @RequestParam(required = false) List<Long> excludeQuestionIds
    ) {
        CouncilReviewQuestionResponse question = questionService.getRandomQuestion(excludeQuestionIds);
        return ResponseFactory.success(CouncilReviewSuccessCode.COUNCIL_REVIEW_QUESTION_RANDOM_SUCCESS, question);
    }
}