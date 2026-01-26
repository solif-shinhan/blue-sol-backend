package com.solif.backend.domain.auth.controller;

import com.solif.backend.domain.auth.dto.AuthSuccessCode;
import com.solif.backend.domain.auth.dto.LoginRequest;
import com.solif.backend.domain.auth.dto.LoginResponse;
import com.solif.backend.domain.auth.dto.SignupRequest;
import com.solif.backend.domain.auth.dto.SignupResponse;
import com.solif.backend.domain.auth.service.AuthService;
import com.solif.backend.global.common.response.ResponseFactory;
import com.solif.backend.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "회원가입/로그인 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
}
