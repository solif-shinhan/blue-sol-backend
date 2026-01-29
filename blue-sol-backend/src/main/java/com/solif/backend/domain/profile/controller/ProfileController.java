package com.solif.backend.domain.profile.controller;

import com.solif.backend.domain.profile.dto.*;
import com.solif.backend.domain.profile.service.ProfileService;
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

@Tag(name = "프로필", description = "온보딩 프로필 API")
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @Operation(
            summary = "프로필 생성",
            description = "온보딩 프로필을 생성합니다. QR코드가 자동 생성됩니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<SuccessResponse<ProfileCreateResponse>> createProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProfileCreateRequest request
    ) {
        ProfileCreateResponse response = profileService.createProfile(userId, request);
        return ResponseFactory.success(ProfileSuccessCode.PROFILE_CREATE_SUCCESS, response);
    }

    @Operation(
            summary = "프로필 수정",
            description = "프로필 정보를 수정합니다. 수정할 필드만 전송합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PatchMapping
    public ResponseEntity<SuccessResponse<ProfileUpdateResponse>> updateProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        ProfileUpdateResponse response = profileService.updateProfile(userId, request);
        return ResponseFactory.success(ProfileSuccessCode.PROFILE_UPDATE_SUCCESS, response);
    }

    @Operation(
            summary = "프로필 조회",
            description = "사용자의 프로필과 관심사를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<SuccessResponse<ProfileResponse>> getProfile(
            @AuthenticationPrincipal Long userId
    ) {
        ProfileResponse response = profileService.getProfile(userId);
        return ResponseFactory.success(ProfileSuccessCode.PROFILE_READ_SUCCESS, response);
    }
}
