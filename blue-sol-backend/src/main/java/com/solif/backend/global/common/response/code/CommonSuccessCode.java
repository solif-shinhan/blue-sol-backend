package com.solif.backend.global.common.response.code;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonSuccessCode implements SuccessCode {

    SUCCESS(HttpStatus.OK, "COMMON_200", "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED, "COMMON_201", "리소스가 성공적으로 생성되었습니다."),
    NO_CONTENT(HttpStatus.NO_CONTENT, "COMMON_204", "요청이 성공적으로 처리되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
