package com.solif.backend.global.common.exception;

import com.solif.backend.domain.auth.exception.AuthErrorCode;
import com.solif.backend.global.common.exception.code.CommonErrorCode;
import com.solif.backend.global.common.response.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));
    }

    // 2. Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        CommonErrorCode errorCode = CommonErrorCode.VALIDATION_ERROR;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), errors));
    }

    // 3. DataIntegrityViolationException 처리 (유니크 제약 위반)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("DataIntegrityViolationException: {}", e.getMessage());

        String message = e.getMessage();
        ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;

        // 에러 메시지에서 컬럼명 추출하여 적절한 에러 코드 반환
        if (message != null) {
            if (message.contains("login_id") || message.contains("LOGIN_ID")) {
                errorCode = AuthErrorCode.DUPLICATE_LOGIN_ID;
            } else if (message.contains("email") || message.contains("EMAIL")) {
                errorCode = AuthErrorCode.DUPLICATE_EMAIL;
            } else if (message.contains("phone") || message.contains("PHONE")) {
                errorCode = AuthErrorCode.DUPLICATE_PHONE;
            } else if (message.contains("scholar_number") || message.contains("SCHOLAR_NUMBER")) {
                errorCode = AuthErrorCode.DUPLICATE_SCHOLAR_NUMBER;
            }
        }

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));
    }

    // 4. 일반 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));
    }
}
