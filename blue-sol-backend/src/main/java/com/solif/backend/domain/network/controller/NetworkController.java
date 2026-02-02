package com.solif.backend.domain.network.controller;

import com.solif.backend.domain.network.dto.*;
import com.solif.backend.domain.network.service.NetworkService;
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

@Tag(name = "교류망", description = "교류망 API")
@RestController
@RequestMapping("/api/v1/networks")
@RequiredArgsConstructor
public class NetworkController {

    private final NetworkService networkService;

    @Operation(
            summary = "나의 교류망 목록 조회",
            description = "나의 교류망에 추가된 사람들을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<SuccessResponse<NetworkListResponse>> getMyNetworks(
            @AuthenticationPrincipal Long userId
    ) {
        NetworkListResponse response = networkService.getMyNetworks(userId);
        return ResponseFactory.success(NetworkSuccessCode.NETWORK_LIST_SUCCESS, response);
    }

    @Operation(
            summary = "교류망 추가",
            description = "사용자 ID로 교류망에 추가합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<SuccessResponse<NetworkAddResponse>> addNetwork(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody NetworkAddRequest request
    ) {
        NetworkAddResponse response = networkService.addNetwork(userId, request);
        return ResponseFactory.success(NetworkSuccessCode.NETWORK_ADD_SUCCESS, response);
    }

    @Operation(
            summary = "QR 코드 스캔으로 교류망 추가",
            description = "QR 코드를 스캔하면 상대방을 나의 교류망에 추가하고, 상대방에게 알림을 발송합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/qr-scan")
    public ResponseEntity<SuccessResponse<NetworkAddResponse>> addNetworkByQrScan(
            @AuthenticationPrincipal Long userId,
            @RequestParam String qrData
    ) {
        NetworkAddResponse response = networkService.addNetworkByQrScan(userId, qrData);
        return ResponseFactory.success(NetworkSuccessCode.NETWORK_QR_ADD_SUCCESS, response);
    }

    @Operation(
            summary = "상호작용 발송",
            description = "응원하기 또는 경험 나누기를 발송합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/interactions")
    public ResponseEntity<SuccessResponse<NetworkInteractionResponse>> sendInteraction(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody NetworkInteractionRequest request
    ) {
        NetworkInteractionResponse response = networkService.sendInteraction(userId, request);
        return ResponseFactory.success(NetworkSuccessCode.NETWORK_INTERACTION_SUCCESS, response);
    }

    @Operation(
            summary = "교류망 추천 조회",
            description = "같은 관심사 기반 추천과 전체 사용자를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/recommendations")
    public ResponseEntity<SuccessResponse<NetworkRecommendationResponse>> getRecommendations(
            @AuthenticationPrincipal Long userId
    ) {
        NetworkRecommendationResponse response = networkService.getRecommendations(userId);
        return ResponseFactory.success(NetworkSuccessCode.NETWORK_RECOMMENDATION_SUCCESS, response);
    }

    @Operation(
            summary = "교류망 검색",
            description = "이름으로 사용자를 검색합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<NetworkSearchResponse>> searchNetworks(
            @AuthenticationPrincipal Long userId,
            @RequestParam String keyword
    ) {
        NetworkSearchResponse response = networkService.searchNetworks(userId, keyword);
        return ResponseFactory.success(NetworkSuccessCode.NETWORK_SEARCH_SUCCESS, response);
    }
}
