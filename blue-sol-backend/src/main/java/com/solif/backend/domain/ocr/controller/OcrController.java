package com.solif.backend.domain.ocr.controller;

import com.solif.backend.domain.ocr.code.OcrSuccessCode;
import com.solif.backend.domain.ocr.dto.request.OcrRequest;
import com.solif.backend.domain.ocr.dto.response.OcrResponse;
import com.solif.backend.domain.ocr.service.OcrService;
import com.solif.backend.global.common.response.ResponseFactory;
import com.solif.backend.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "OCR", description = "OCR API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;

    @Operation(
            summary = "영수증 금액 추출",
            description = "파일 ID를 받아 S3에서 영수증 이미지를 다운로드하고 OCR로 금액을 추출합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/receipts/ocr")
    public ResponseEntity<SuccessResponse<OcrResponse>> extractReceiptAmount(
            @Valid @RequestBody OcrRequest request
    ) {
        OcrResponse response = ocrService.processOcr(request.getFileId());
        return ResponseFactory.success(OcrSuccessCode.OCR_AMOUNT_EXTRACT_SUCCESS, response);
    }
}