package com.solif.backend.global.common.response;

import com.solif.backend.global.common.exception.ErrorCode;
import org.springframework.http.ResponseEntity;

public class ResponseFactory {

    // 성공 응답 (데이터 있음)
    public static <T> ResponseEntity<SuccessResponse<T>> success(SuccessCode successCode, T data) {
        return ResponseEntity
                .status(successCode.getStatus())
                .body(new SuccessResponse<>(successCode, data));
    }

    // 성공 응답 (데이터 없음)
    public static ResponseEntity<SuccessResponse<Void>> success(SuccessCode successCode) {
        return ResponseEntity
                .status(successCode.getStatus())
                .body(new SuccessResponse<>(successCode, null));
    }

    // 에러 응답
    public static ResponseEntity<ErrorResponse> error(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));
    }
}
