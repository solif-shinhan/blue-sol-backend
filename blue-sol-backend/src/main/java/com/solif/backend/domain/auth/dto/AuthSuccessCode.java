package com.solif.backend.domain.auth.dto;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthSuccessCode implements SuccessCode {

    SIGNUP_SUCCESS(HttpStatus.CREATED, "AUTH_201", "회원가입이 완료되었습니다."),
    LOGIN_SUCCESS(HttpStatus.OK, "AUTH_200", "로그인이 성공적으로 완료되었습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "AUTH_200", "로그아웃이 완료되었습니다."),
    TOKEN_REFRESH_SUCCESS(HttpStatus.OK, "AUTH_200", "토큰이 갱신되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
