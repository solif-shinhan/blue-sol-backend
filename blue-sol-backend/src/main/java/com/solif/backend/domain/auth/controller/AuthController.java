package com.solif.backend.domain.auth.controller;

import com.solif.backend.domain.auth.dto.*;
import com.solif.backend.domain.auth.exception.AuthErrorCode;
import com.solif.backend.domain.auth.service.AuthService;
import com.solif.backend.global.common.exception.CustomException;
import com.solif.backend.global.common.response.ResponseFactory;
import com.solif.backend.global.common.response.SuccessResponse;
import com.solif.backend.global.jwt.JwtUtil;
import com.solif.backend.global.jwt.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "회원가입/로그인 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;


    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<SignupResponse>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        SignupResponse response = authService.signup(request);
        return ResponseFactory.success(AuthSuccessCode.SIGNUP_SUCCESS, response);
    }

    @Operation(summary = "로그인", description = "사용자 로그인을 수행합니다.")
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ResponseFactory.success(AuthSuccessCode.LOGIN_SUCCESS, response);
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 수행합니다.")
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // Bearer 토큰 검증 및 추출
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CustomException(AuthErrorCode.INVALID_TOKEN);
        }
        String token = authorizationHeader.substring(7);

        // 토큰 유효성 검증
        if (!jwtUtil.validateToken(token)) {
            throw new CustomException(AuthErrorCode.INVALID_TOKEN);
        }

        // 토큰 만료 시간 가져오기
        long expirationTime = jwtUtil.getExpirationFromToken(token);

        // 블랙리스트에 추가
        tokenBlacklistService.addToBlacklist(token, expirationTime);

        return ResponseFactory.success(AuthSuccessCode.LOGOUT_SUCCESS, null);
    }
}
