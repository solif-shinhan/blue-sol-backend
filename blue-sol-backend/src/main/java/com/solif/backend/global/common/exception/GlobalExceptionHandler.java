package com.solif.backend.global.common.exception;

import com.solif.backend.domain.auth.code.AuthErrorCode;
import com.solif.backend.global.common.exception.code.CommonErrorCode;
import com.solif.backend.global.common.response.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

    // 3. @Validated + @RequestParam 검증 예외 처리 (ConstraintViolationException)
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            jakarta.validation.ConstraintViolationException e) {
        Map<String, String> errors = e.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (a, b) -> a
                ));

        CommonErrorCode errorCode = CommonErrorCode.VALIDATION_ERROR;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), errors));
    }

    // 4. DataIntegrityViolationException 처리 (3단계 방어)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        
        // Root Cause 추출
        Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(e);

        // 1단계: SQLState 기반 확인 (가장 안정적)
        if (rootCause instanceof SQLException sqlEx) {
            String sqlState = sqlEx.getSQLState();
            
            // UNIQUE 제약 위반 확인
            // 23505: PostgreSQL, 23000: MySQL/H2
            if ("23505".equals(sqlState) || "23000".equals(sqlState)) {
                
                // 2단계: ConstraintName 기반 매핑
                if (e.getCause() instanceof ConstraintViolationException cve) {
                    String constraintName = cve.getConstraintName();
                    if (constraintName != null) {
                        errorCode = mapConstraintToErrorCode(constraintName);
                        
                        // 제약조건 이름만 로그 (개인정보 노출 방지)
                        if (errorCode != CommonErrorCode.INTERNAL_SERVER_ERROR) {
                            log.warn("Unique constraint violation: constraint={}", constraintName);
                            return ResponseEntity
                                    .status(errorCode.getStatus())
                                    .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));
                        }
                    }
                }
                
                // 3단계: 메시지 기반 매핑 (폴백)
                errorCode = mapMessageToErrorCode(e.getMessage());
                if (errorCode != CommonErrorCode.INTERNAL_SERVER_ERROR) {
                    log.warn("Unique constraint violation detected via message parsing");
                    return ResponseEntity
                            .status(errorCode.getStatus())
                            .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));
                }
            }
        }

        // 4. 알 수 없는 데이터 무결성 위반
        log.error("DataIntegrityViolationException: sqlState={}", 
                rootCause instanceof SQLException ? ((SQLException) rootCause).getSQLState() : "unknown");
        
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));
    }

    /**
     * 제약조건 이름 → 에러 코드 매핑
     */
    private ErrorCode mapConstraintToErrorCode(String constraintName) {
        return switch (constraintName.toLowerCase()) {
            case "uk_user_login_id" -> AuthErrorCode.DUPLICATE_LOGIN_ID;
            case "uk_user_email" -> AuthErrorCode.DUPLICATE_EMAIL;
            case "uk_user_phone" -> AuthErrorCode.DUPLICATE_PHONE;
            case "uk_user_scholar_number" -> AuthErrorCode.DUPLICATE_SCHOLAR_NUMBER;
            default -> CommonErrorCode.INTERNAL_SERVER_ERROR;
        };
    }

    /**
     * 메시지 파싱 → 에러 코드 매핑 (폴백)
     */
    private ErrorCode mapMessageToErrorCode(String message) {
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            if (lowerMessage.contains("login_id")) {
                return AuthErrorCode.DUPLICATE_LOGIN_ID;
            }
            if (lowerMessage.contains("email")) {
                return AuthErrorCode.DUPLICATE_EMAIL;
            }
            if (lowerMessage.contains("phone")) {
                return AuthErrorCode.DUPLICATE_PHONE;
            }
            if (lowerMessage.contains("scholar_number")) {
                return AuthErrorCode.DUPLICATE_SCHOLAR_NUMBER;
            }
        }
        return CommonErrorCode.INTERNAL_SERVER_ERROR;
    }

    // 5. Enum 타입 변환 실패 예외 처리 (잘못된 @RequestParam 값)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        
        String paramName = e.getName();
        String invalidValue = e.getValue() != null ? e.getValue().toString() : "null";
        Class<?> requiredType = e.getRequiredType();
        
        String message;
        if (requiredType != null && requiredType.isEnum()) {
            // Enum 타입인 경우 허용 가능한 값 목록 제공
            Object[] enumConstants = requiredType.getEnumConstants();
            String allowedValues = java.util.Arrays.stream(enumConstants)
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            message = String.format("'%s' 파라미터의 값 '%s'이(가) 유효하지 않습니다. 허용 값: [%s]",
                    paramName, invalidValue, allowedValues);
        } else {
            message = String.format("'%s' 파라미터의 값 '%s'이(가) 유효하지 않습니다.", 
                    paramName, invalidValue);
        }
        
        log.warn("MethodArgumentTypeMismatch: param={}, value={}, requiredType={}", 
                paramName, invalidValue, requiredType);
        
        CommonErrorCode errorCode = CommonErrorCode.VALIDATION_ERROR;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getCode(), message));
    }

    // 6. 일반 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("Unexpected exception", e);
        CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));
    }
}
