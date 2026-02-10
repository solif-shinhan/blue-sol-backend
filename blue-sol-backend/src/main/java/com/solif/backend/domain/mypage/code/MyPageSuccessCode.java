package com.solif.backend.domain.mypage.code;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MyPageSuccessCode implements SuccessCode {

    MYPAGE_READ_SUCCESS(HttpStatus.OK, "MYPAGE_200", "마이페이지 조회 성공");

    private final HttpStatus status;
    private final String code;
    private final String message;
}