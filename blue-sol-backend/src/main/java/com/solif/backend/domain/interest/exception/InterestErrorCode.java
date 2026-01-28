package com.solif.backend.domain.interest.exception;

import com.solif.backend.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InterestErrorCode implements ErrorCode {

    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "INTEREST_001", "유효하지 않은 관심사 카테고리입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "INTEREST_002", "사용자를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
