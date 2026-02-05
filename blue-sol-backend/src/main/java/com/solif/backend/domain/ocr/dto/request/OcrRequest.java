package com.solif.backend.domain.ocr.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "OCR 요청 (운영용)")
public class OcrRequest {

    @Schema(description = "파일 ID", example = "123", required = true)
    @NotNull(message = "파일 ID는 필수입니다.")
    private Long fileId;
}