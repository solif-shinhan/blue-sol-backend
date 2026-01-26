package com.solif.backend.domain.auth.exception;

import com.solif.backend.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    // 인증 관련 에러
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_001", "아이디 또는 비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_002", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "만료된 토큰입니다."),
    
    // 회원가입 관련 에러
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "AUTH_101", "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH_102", "이미 사용 중인 이메일입니다."),
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "AUTH_103", "이미 사용 중인 전화번호입니다."),
    DUPLICATE_SCHOLAR_NUMBER(HttpStatus.CONFLICT, "AUTH_104", "이미 등록된 학번입니다."),
    
    // 사용자 조회 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_201", "사용자를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
