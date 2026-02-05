package com.solif.backend.domain.ocr.code;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OcrSuccessCode implements SuccessCode {

    // 테스트 OCR
    OCR_TEST_SUCCESS(HttpStatus.OK, "OCR_200", "OCR 테스트 처리에 성공했습니다."),

    // 운영 OCR
    OCR_AMOUNT_EXTRACT_SUCCESS(HttpStatus.OK, "OCR_201", "영수증 금액 추출에 성공했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}