package com.solif.backend.global.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Map;

@Getter
@Schema(description = "에러 응답 포맷")
public class ErrorResponse {

    @Schema(description = "요청 성공 여부", example = "false")
    private final boolean isSuccess = false;

    @Schema(description = "에러 코드", example = "AUTH_401")
    private final String code;

    @Schema(description = "에러 메시지", example = "비밀번호가 일치하지 않습니다.")
    private final String message;

    @Schema(description = "세부 에러 정보 (Validation 에러 시)", nullable = true)
    private final Map<String, String> errors;

    // 기본 생성자 (세부 에러 없음)
    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
        this.errors = null;
    }

    // Validation 에러용 생성자 (세부 에러 포함)
    public ErrorResponse(String code, String message, Map<String, String> errors) {
        this.code = code;
        this.message = message;
        this.errors = errors;
    }
}
