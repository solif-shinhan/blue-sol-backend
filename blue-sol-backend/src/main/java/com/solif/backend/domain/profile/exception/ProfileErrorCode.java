package com.solif.backend.domain.profile.exception;

import com.solif.backend.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProfileErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "PROFILE_001", "사용자를 찾을 수 없습니다."),
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "PROFILE_002", "프로필을 찾을 수 없습니다."),
    PROFILE_ALREADY_EXISTS(HttpStatus.CONFLICT, "PROFILE_003", "이미 프로필이 존재합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
