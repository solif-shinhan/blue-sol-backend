package com.solif.backend.domain.ocr.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "OCR 테스트 응답 (상세 정보)")
public class OcrTestResponse {

    @Schema(description = "추출된 최종 금액 (가장 큰 값)", example = "13200", nullable = true)
    private Long amount;

    @Schema(description = "추출된 모든 금액 후보", example = "[13200, 13000]")
    private List<Long> candidates;

    @Schema(description = "OCR로 추출된 전체 텍스트", example = "합계 13,200원\\n부가세 포함")
    private String rawText;

    public static OcrTestResponse of(Long amount, List<Long> candidates, String rawText) {
        return OcrTestResponse.builder()
                .amount(amount)
                .candidates(candidates)
                .rawText(rawText)
                .build();
    }
}