package com.solif.backend.domain.interest.controller;

import com.solif.backend.domain.interest.dto.InterestRequest;
import com.solif.backend.domain.interest.dto.InterestResponse;
import com.solif.backend.domain.interest.dto.InterestSuccessCode;
import com.solif.backend.domain.interest.service.InterestService;
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

@Tag(name = "관심사", description = "관심사 등록 API")
@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    @Operation(
            summary = "관심사 등록",
            description = "사용자의 관심사를 등록합니다. 최소 2개 이상 선택해야 합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<SuccessResponse<InterestResponse>> registerInterests(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody InterestRequest request
    ) {
        InterestResponse response = interestService.registerInterests(userId, request);
        return ResponseFactory.success(InterestSuccessCode.INTEREST_REGISTER_SUCCESS, response);
    }
}
