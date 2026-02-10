package com.solif.backend.domain.mypage.controller;

import com.solif.backend.domain.mypage.code.MyPageSuccessCode;
import com.solif.backend.domain.mypage.dto.MyPageResponse;
import com.solif.backend.domain.mypage.service.MyPageService;
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
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "마이페이지 API", description = "마이페이지 조회 API")
@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @Operation(
            summary = "마이페이지 조회",
            description = "사용자의 이름, SOLID 목표, 활동 대시보드(점수/타입), 나의 지난 활동(자치회 활동 후기 최신 3개)을 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping
    public ResponseEntity<SuccessResponse<MyPageResponse>> getMyPage(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        MyPageResponse response = myPageService.getMyPage(userId);
        return ResponseFactory.success(MyPageSuccessCode.MYPAGE_READ_SUCCESS, response);
    }
}