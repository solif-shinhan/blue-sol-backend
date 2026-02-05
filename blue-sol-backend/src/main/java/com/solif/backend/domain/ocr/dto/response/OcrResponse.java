package com.solif.backend.domain.ocr.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "OCR 응답 (운영용)")
public class OcrResponse {

    @Schema(description = "추출된 금액", example = "13200", nullable = true)
    private Long amount;

    public static OcrResponse of(Long amount) {
        return OcrResponse.builder()
                .amount(amount)
                .build();
    }
}