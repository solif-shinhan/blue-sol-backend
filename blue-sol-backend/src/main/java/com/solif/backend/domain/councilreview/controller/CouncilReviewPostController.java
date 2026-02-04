package com.solif.backend.domain.councilreview.controller;

import com.solif.backend.domain.councilreview.code.CouncilReviewSuccessCode;
import com.solif.backend.domain.councilreview.dto.request.CouncilReviewPostCreateRequest;
import com.solif.backend.domain.councilreview.dto.request.CouncilReviewPostUpdateRequest;
import com.solif.backend.domain.councilreview.dto.response.CouncilReviewPostCreateResponse;
import com.solif.backend.domain.councilreview.dto.response.CouncilReviewPostDetailResponse;
import com.solif.backend.domain.councilreview.dto.response.CouncilReviewPostListResponse;
import com.solif.backend.domain.councilreview.service.CouncilReviewPostService;
import com.solif.backend.global.common.response.ResponseFactory;
import com.solif.backend.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "자치회 활동 후기", description = "자치회 활동 후기 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CouncilReviewPostController {

    private final CouncilReviewPostService reviewPostService;

    @Operation(
            summary = "자치회 활동 후기 목록 조회",
            description = "특정 자치회의 활동 후기 목록을 페이징하여 조회합니다. 모든 인증 사용자가 조회 가능합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/councils/{councilId}/review-posts")
    public ResponseEntity<SuccessResponse<Slice<CouncilReviewPostListResponse>>> getCouncilReviewPosts(
            @PathVariable Long councilId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Slice<CouncilReviewPostListResponse> posts = reviewPostService.getCouncilReviewPosts(councilId, pageable);
        return ResponseFactory.success(CouncilReviewSuccessCode.COUNCIL_REVIEW_LIST_SUCCESS, posts);
    }

    @Operation(
            summary = "자치회 활동 후기 상세 조회",
            description = "활동 후기 상세 정보 및 모든 릴레이 글을 조회합니다. 조회수가 1 증가합니다. 모든 인증 사용자가 조회 가능합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/council-review-posts/{councilReviewPostId}")
    public ResponseEntity<SuccessResponse<CouncilReviewPostDetailResponse>> getCouncilReviewPostDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long councilReviewPostId
    ) {
        CouncilReviewPostDetailResponse post = reviewPostService.getCouncilReviewPostDetail(userId, councilReviewPostId);
        return ResponseFactory.success(CouncilReviewSuccessCode.COUNCIL_REVIEW_DETAIL_SUCCESS, post);
    }

    @Operation(
            summary = "자치회 활동 후기 생성",
            description = "리더가 활동 후기를 생성하고 첫 번째 릴레이 글을 작성합니다. 리더만 생성 가능합니다. 예산이 자동으로 차감됩니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/councils/{councilId}/review-posts")
    public ResponseEntity<SuccessResponse<CouncilReviewPostCreateResponse>> createCouncilReviewPost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long councilId,
            @Valid @RequestBody CouncilReviewPostCreateRequest request
    ) {
        CouncilReviewPostCreateResponse response = reviewPostService.createCouncilReviewPost(
                userId, councilId, request
        );
        return ResponseFactory.success(CouncilReviewSuccessCode.COUNCIL_REVIEW_CREATE_SUCCESS, response);
    }

    @Operation(
            summary = "자치회 활동 후기 수정",
            description = "리더가 활동 후기 메타 정보를 수정합니다 (제목, 날짜, 장소, 비용, 이미지, 참여자). 리더만 수정 가능합니다. 예산이 재계산됩니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PatchMapping("/council-review-posts/{councilReviewPostId}")
    public ResponseEntity<SuccessResponse<Void>> updateCouncilReviewPost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long councilReviewPostId,
            @Valid @RequestBody CouncilReviewPostUpdateRequest request
    ) {
        reviewPostService.updateCouncilReviewPost(userId, councilReviewPostId, request);
        return ResponseFactory.success(CouncilReviewSuccessCode.COUNCIL_REVIEW_UPDATE_SUCCESS);
    }

    @Operation(
            summary = "자치회 활동 후기 삭제",
            description = "리더가 활동 후기 전체를 삭제합니다 (Soft Delete). 리더만 삭제 가능합니다. 모든 릴레이와 참여자 정보가 함께 삭제되고 예산이 복구됩니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @DeleteMapping("/council-review-posts/{councilReviewPostId}")
    public ResponseEntity<SuccessResponse<Void>> deleteCouncilReviewPost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long councilReviewPostId
    ) {
        reviewPostService.deleteCouncilReviewPost(userId, councilReviewPostId);
        return ResponseFactory.success(CouncilReviewSuccessCode.COUNCIL_REVIEW_DELETE_SUCCESS);
    }
}