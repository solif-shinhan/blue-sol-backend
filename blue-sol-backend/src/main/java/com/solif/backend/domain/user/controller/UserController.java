package com.solif.backend.domain.user.controller;

import com.solif.backend.domain.user.dto.UserMeResponse;
import com.solif.backend.domain.user.dto.UserSuccessCode;
import com.solif.backend.domain.user.service.UserService;
import com.solif.backend.global.common.response.ResponseFactory;
import com.solif.backend.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자", description = "테스트용 사용자 정보 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "토큰 테스트용 내 정보 조회",
            description = "현재 로그인한 사용자의 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<UserMeResponse>> getMyInfo(
            @AuthenticationPrincipal Long userId
    ) {
        UserMeResponse response = userService.getMyInfo(userId);
        return ResponseFactory.success(UserSuccessCode.GET_MY_INFO_SUCCESS, response);
    }
}
