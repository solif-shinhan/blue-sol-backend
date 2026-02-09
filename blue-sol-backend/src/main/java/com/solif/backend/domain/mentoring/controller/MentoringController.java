package com.solif.backend.domain.mentoring.controller;

import com.solif.backend.domain.mentoring.code.MentoringSuccessCode;
import com.solif.backend.domain.mentoring.dto.*;
import com.solif.backend.domain.mentoring.service.MentoringService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "멘토링", description = "멘토링 API")
@RestController
@RequestMapping("/api/v1/mentoring")
@RequiredArgsConstructor
public class MentoringController {

    private final MentoringService mentoringService;

    @Operation(
            summary = "전문가 멘토 목록 조회",
            description = "활성화된 전문가 멘토 목록을 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/mentors")
    public ResponseEntity<SuccessResponse<List<MentorListResponse>>> getMentors(
            @AuthenticationPrincipal Long userId
    ) {
        List<MentorListResponse> response = mentoringService.getMentors();
        return ResponseFactory.success(MentoringSuccessCode.MENTOR_LIST_SUCCESS, response);
    }

    @Operation(
            summary = "멘토링 신청",
            description = "전문가 멘토에게 멘토링을 신청합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/requests")
    public ResponseEntity<SuccessResponse<MentoringRequestCreateResponse>> createMentoringRequest(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody MentoringRequestCreateRequest request
    ) {
        MentoringRequestCreateResponse response = mentoringService.createMentoringRequest(userId, request);
        return ResponseFactory.success(MentoringSuccessCode.MENTORING_REQUEST_CREATE_SUCCESS, response);
    }

    @Operation(
            summary = "내가 보낸 신청서 목록 조회",
            description = "내가 전문가 멘토에게 보낸 멘토링 신청서 목록을 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/requests/sent")
    public ResponseEntity<SuccessResponse<Slice<MentoringRequestListResponse>>> getSentRequests(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Slice<MentoringRequestListResponse> response = mentoringService.getSentRequests(userId, pageable);
        return ResponseFactory.success(MentoringSuccessCode.MENTORING_REQUEST_SENT_LIST_SUCCESS, response);
    }

    @Operation(
            summary = "멘토링 신청서 상세 조회",
            description = "멘토링 신청서의 상세 내용을 조회합니다. 신청자 본인만 조회 가능합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/requests/{requestId}")
    public ResponseEntity<SuccessResponse<MentoringRequestDetailResponse>> getRequestDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long requestId
    ) {
        MentoringRequestDetailResponse response = mentoringService.getRequestDetail(userId, requestId);
        return ResponseFactory.success(MentoringSuccessCode.MENTORING_REQUEST_DETAIL_SUCCESS, response);
    }

    @Operation(
            summary = "내가 받은 답변 목록 조회",
            description = "내가 보낸 멘토링 신청서 중 관리자가 답변을 작성한 신청서 목록을 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/requests/received")
    public ResponseEntity<SuccessResponse<Slice<MentoringRequestListResponse>>> getReceivedRequests(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Slice<MentoringRequestListResponse> response = mentoringService.getReceivedRequests(userId, pageable);
        return ResponseFactory.success(MentoringSuccessCode.MENTORING_REQUEST_RECEIVED_LIST_SUCCESS, response);
    }

    @Operation(
            summary = "멘토링 홈 조회",
            description = "멘토링 홈 화면 정보를 조회합니다. (전문가 멘토, 선후배 멘토링, 후기 목록)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/home")
    public ResponseEntity<SuccessResponse<MentoringHomeResponse>> getMentoringHome(
            @AuthenticationPrincipal Long userId
    ) {
        MentoringHomeResponse response = mentoringService.getMentoringHome(userId);
        return ResponseFactory.success(MentoringSuccessCode.MENTORING_HOME_SUCCESS, response);
    }

    @Operation(
            summary = "멘토링 엽서 발송",
            description = "관리자에게 멘토링 엽서를 발송합니다. 전문가 멘토 중 맘에 드는 사람이 없을 때 사용합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/cards")
    public ResponseEntity<SuccessResponse<MentoringCardSendResponse>> sendMentoringCard(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody MentoringCardSendRequest request
    ) {
        MentoringCardSendResponse response = mentoringService.sendMentoringCard(userId, request);
        return ResponseFactory.success(MentoringSuccessCode.MENTORING_CARD_SEND_SUCCESS, response);
    }

    @Operation(
            summary = "보낸 멘토링 엽서 목록 조회",
            description = "내가 관리자에게 보낸 멘토링 엽서 목록을 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/cards/sent")
    public ResponseEntity<SuccessResponse<Slice<MentoringCardListResponse>>> getSentCards(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Slice<MentoringCardListResponse> response = mentoringService.getSentCards(userId, pageable);
        return ResponseFactory.success(MentoringSuccessCode.MENTORING_CARD_SENT_LIST_SUCCESS, response);
    }

    @Operation(
            summary = "멘토링 엽서 상세 조회",
            description = "멘토링 엽서 상세 내용을 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/cards/{cardId}")
    public ResponseEntity<SuccessResponse<MentoringCardDetailResponse>> getCardDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long cardId
    ) {
        MentoringCardDetailResponse response = mentoringService.getCardDetail(userId, cardId);
        return ResponseFactory.success(MentoringSuccessCode.MENTORING_CARD_DETAIL_SUCCESS, response);
    }
}