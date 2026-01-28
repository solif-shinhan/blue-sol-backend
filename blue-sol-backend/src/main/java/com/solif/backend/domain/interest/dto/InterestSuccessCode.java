package com.solif.backend.domain.interest.dto;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InterestSuccessCode implements SuccessCode {

    INTEREST_REGISTER_SUCCESS(HttpStatus.OK, "INTEREST_S001", "관심사 등록에 성공했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
