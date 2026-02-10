package com.solif.backend.domain.scholarshipprogrampost.code;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ScholarshipProgramSuccessCode implements SuccessCode {

    SCHOLARSHIP_PROGRAM_LIST_SUCCESS(HttpStatus.OK, "SCHOLARSHIP_PROGRAM_200", "장학 프로그램 목록 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}