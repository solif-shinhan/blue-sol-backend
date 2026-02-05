package com.solif.backend.domain.ocr.controller;

import com.solif.backend.domain.ocr.code.OcrSuccessCode;
import com.solif.backend.domain.ocr.dto.response.OcrTestResponse;
import com.solif.backend.domain.ocr.service.OcrService;
import com.solif.backend.global.common.response.ResponseFactory;
import com.solif.backend.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "OCR 테스트", description = "OCR 테스트 API (개발용)")
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class OcrTestController {

    private final OcrService ocrService;

    @Operation(
            summary = "OCR 테스트",
            description = "이미지 파일을 직접 업로드하여 OCR 테스트를 수행합니다. 추출된 텍스트, 금액 후보, 최종 금액을 모두 반환합니다."
    )
    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<OcrTestResponse>> testOcr(
            @RequestParam("file") MultipartFile file
    ) {
        OcrTestResponse response = ocrService.processOcrTest(file);
        return ResponseFactory.success(OcrSuccessCode.OCR_TEST_SUCCESS, response);
    }
}