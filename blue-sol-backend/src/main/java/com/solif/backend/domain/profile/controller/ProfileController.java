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

import java.util.List;

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

    @Operation(
            summary = "온보딩 배경화면 리스트 조회",
            description = "선택 가능한 카드 배경화면 리스트를 조회합니다."
    )
    @GetMapping("/backgrounds")
    public ResponseEntity<SuccessResponse<List<BackgroundListResponse>>> getBackgrounds() {
        List<BackgroundListResponse> response = profileService.getBackgroundList();
        return ResponseFactory.success(ProfileSuccessCode.PROFILE_READ_SUCCESS, response);
    }

    @Operation(
            summary = "Solid 배경화면 리스트 조회",
            description = "선택 가능한 카드 배경화면 리스트를 조회합니다."
    )
    @GetMapping("/solidbgs")
    public ResponseEntity<SuccessResponse<List<BackgroundListResponse>>> getSOLBackgrounds() {
        List<BackgroundListResponse> response = profileService.getSOLBackgroundsList();
        return ResponseFactory.success(ProfileSuccessCode.PROFILE_READ_SUCCESS, response);
    }

    @Operation(
            summary = "Solid Qr 배경화면 리스트 조회",
            description = "선택 가능한 카드 배경화면 리스트를 조회합니다."
    )
    @GetMapping("/solidqrbgs")
    public ResponseEntity<SuccessResponse<List<BackgroundListResponse>>> getSOLQRBackgrounds() {
        List<BackgroundListResponse> response = profileService.getSOLQRBackgroundsList();
        return ResponseFactory.success(ProfileSuccessCode.PROFILE_READ_SUCCESS, response);
    }

    @Operation(
            summary = "교류망 프로필 배경화면 리스트 조회",
            description = "선택 가능한 카드 배경화면 리스트를 조회합니다."
    )
    @GetMapping("/profilebgs")
    public ResponseEntity<SuccessResponse<List<BackgroundListResponse>>> getProfileBackgrounds() {
        List<BackgroundListResponse> response = profileService.getProfileBackgroundsList();
        return ResponseFactory.success(ProfileSuccessCode.PROFILE_READ_SUCCESS, response);
    }

    @Operation(
            summary = "마이페이지 배경화면 리스트 조회",
            description = "선택 가능한 카드 배경화면 리스트를 조회합니다."
    )
    @GetMapping("/mypagebgs")
    public ResponseEntity<SuccessResponse<List<BackgroundListResponse>>> getMyPageBackgrounds() {
        List<BackgroundListResponse> response = profileService.getMyPageBackgroundsList();
        return ResponseFactory.success(ProfileSuccessCode.PROFILE_READ_SUCCESS, response);
    }

    @Operation(
            summary = "캐릭터 리스트 조회",
            description = "선택 가능한 카드 배경화면 리스트를 조회합니다."
    )
    @GetMapping("/characters")
    public ResponseEntity<SuccessResponse<List<CharacterListResponse>>> getCharacters() {
        List<CharacterListResponse> response = profileService.getCharacterList();
        return ResponseFactory.success(ProfileSuccessCode.PROFILE_READ_SUCCESS, response);
    }
}
