package com.solif.backend.domain.ocr.service;

import com.solif.backend.domain.file.entity.File;
import com.solif.backend.domain.file.code.FileException;
import com.solif.backend.domain.file.repository.FileRepository;
import com.solif.backend.domain.ocr.code.OcrErrorCode;
import com.solif.backend.domain.ocr.dto.response.OcrResponse;
import com.solif.backend.domain.ocr.dto.response.OcrTestResponse;
import com.solif.backend.domain.ocr.util.AmountParser;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.util.List;

import static com.solif.backend.domain.file.code.FileException.FileErrorCode.FILE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OcrService {

    private final GoogleVisionClient visionClient;
    private final FileRepository fileRepository;
    private final S3Client s3Client;

    // 테스트용 OCR: MultipartFile에서 직접 텍스트 추출
    public OcrTestResponse processOcrTest(MultipartFile file) {
        validateFile(file);

        try {
            // 파일 읽기
            byte[] imageBytes = file.getBytes();

            // OCR 처리
            String rawText = visionClient.extractText(imageBytes);

            // 금액 파싱
            List<Long> candidates = AmountParser.extractAllAmounts(rawText);
            Long amount = candidates.isEmpty() ? null : candidates.get(candidates.size() - 1);

            return OcrTestResponse.of(amount, candidates, rawText);

        } catch (IOException e) {
            log.error("파일 읽기 실패", e);
            throw new CustomException(OcrErrorCode.FILE_READ_FAILED);
        }
    }

    // 운영용 OCR: fileId로 S3에서 파일 다운로드 후 금액만 추출
    public OcrResponse processOcr(Long fileId) {
        // File 엔티티 조회
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileException(FILE_NOT_FOUND));

        // S3에서 파일 다운로드
        byte[] imageBytes = downloadFromS3(file);

        // OCR 처리
        String rawText = visionClient.extractText(imageBytes);

        // 금액 파싱
        Long amount = AmountParser.extractAmount(rawText);

        if (amount == null) {
            throw new CustomException(OcrErrorCode.AMOUNT_NOT_FOUND);
        }

        return OcrResponse.of(amount);
    }

    // S3에서 파일 다운로드
    private byte[] downloadFromS3(File file) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(file.getBucket())
                    .key(file.getObjectKey())
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);

            log.info("S3 파일 다운로드 완료 - Bucket: {}, Key: {}", file.getBucket(), file.getObjectKey());

            return objectBytes.asByteArray();

        } catch (Exception e) {
            log.error("S3 파일 다운로드 실패 - Bucket: {}, Key: {}", file.getBucket(), file.getObjectKey(), e);
            throw new CustomException(OcrErrorCode.FILE_READ_FAILED);
        }
    }

    // 파일 유효성 검증
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(OcrErrorCode.FILE_NOT_PROVIDED);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(OcrErrorCode.INVALID_FILE_FORMAT);
        }
    }
}