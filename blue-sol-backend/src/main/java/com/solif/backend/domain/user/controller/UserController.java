package com.solif.backend.domain.user.controller;

import com.solif.backend.domain.user.dto.UserMeResponse;
import com.solif.backend.domain.user.dto.UserSearchResponse;
import com.solif.backend.domain.user.dto.UserSuccessCode;
import com.solif.backend.domain.user.service.UserService;
import com.solif.backend.global.common.response.ResponseFactory;
import com.solif.backend.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "사용자", description = "테스트용 사용자 정보 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "토큰 테스트용 내 정보 조회",
            description = "현재 로그인한 사용자의 정보를 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<UserMeResponse>> getMyInfo(
            @AuthenticationPrincipal Long userId
    ) {
        UserMeResponse response = userService.getMyInfo(userId);
        return ResponseFactory.success(UserSuccessCode.GET_MY_INFO_SUCCESS, response);
    }

    @Operation(
            summary = "사용자 검색",
            description = "이름으로 사용자를 검색합니다. 자치회 소속 여부도 함께 반환합니다. (최대 20명)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<List<UserSearchResponse>>> searchUsers(
            @Parameter(description = "검색 키워드 (이름)", required = true, example = "김지환")
            @RequestParam String keyword
    ) {
        List<UserSearchResponse> response = userService.searchUsers(keyword);
        return ResponseFactory.success(UserSuccessCode.USER_SEARCH_SUCCESS, response);
    }
}
