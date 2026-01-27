package com.solif.backend.domain.user.dto;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserSuccessCode implements SuccessCode {
    
    GET_MY_INFO_SUCCESS(HttpStatus.OK, "USER_001", "내 정보 조회 성공");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
