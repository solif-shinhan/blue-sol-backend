package com.solif.backend.domain.ocr.code;

import com.solif.backend.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OcrErrorCode implements ErrorCode {

    // 파일 관련
    FILE_NOT_PROVIDED(HttpStatus.BAD_REQUEST, "OCR_001", "파일이 제공되지 않았습니다."),
    FILE_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OCR_002", "파일 읽기에 실패했습니다."),
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "OCR_003", "지원하지 않는 파일 형식입니다. 이미지 파일만 가능합니다."),

    // OCR 처리 관련
    OCR_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OCR_004", "OCR 처리 중 오류가 발생했습니다."),
    NO_TEXT_DETECTED(HttpStatus.BAD_REQUEST, "OCR_005", "이미지에서 텍스트를 감지할 수 없습니다."),
    AMOUNT_NOT_FOUND(HttpStatus.BAD_REQUEST, "OCR_006", "영수증에서 금액을 찾을 수 없습니다."),

    // Vision API 관련
    VISION_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "OCR_007", "Google Vision API 호출에 실패했습니다."),
    VISION_CREDENTIALS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "OCR_008", "Google Vision API 인증에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}