package com.solif.backend.domain.ocr.service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.solif.backend.domain.ocr.code.OcrErrorCode;
import com.solif.backend.global.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class GoogleVisionClient {

    /**
     * Google Vision API를 사용하여 이미지에서 텍스트 추출
     * @param imageBytes 이미지 바이트 배열
     * @return 추출된 전체 텍스트
     */
    public String extractText(byte[] imageBytes) {
        // 1. Vision API 클라이언트 생성 (try-with-resources로 자동 close)
        try (ImageAnnotatorClient visionClient = ImageAnnotatorClient.create()) {

            // 2. 이미지 바이트 배열을 Google Vision 형식으로 변환
            ByteString byteString = ByteString.copyFrom(imageBytes);
            Image image = Image.newBuilder().setContent(byteString).build();

            // 3. "TEXT_DETECTION" 기능 요청 설정
            Feature feature = Feature.newBuilder()
                    .setType(Feature.Type.TEXT_DETECTION)
                    .build();

            // 4. 요청 객체 생성
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(image)
                    .build();

            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request);

            // 5. API 호출 (배치 처리 - 여러 이미지 동시 처리 가능)
            BatchAnnotateImagesResponse response = visionClient.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            if (responses.isEmpty()) {
                log.error("Google Vision API 응답이 비어있습니다.");
                throw new CustomException(OcrErrorCode.VISION_API_ERROR);
            }

            AnnotateImageResponse imageResponse = responses.get(0);

            // 에러 체크
            if (imageResponse.hasError()) {
                String errorMessage = imageResponse.getError().getMessage();
                log.error("Google Vision API 에러: {}", errorMessage);
                throw new CustomException(OcrErrorCode.VISION_API_ERROR);
            }

            // 텍스트 추출
            if (imageResponse.getTextAnnotationsCount() == 0) {
                log.warn("이미지에서 텍스트를 감지할 수 없습니다.");
                throw new CustomException(OcrErrorCode.NO_TEXT_DETECTED);
            }

            // 6. 응답에서 텍스트 추출
            //    textAnnotations[0] = 전체 텍스트
            //    textAnnotations[1~N] = 개별 단어들
            String fullText = imageResponse.getTextAnnotations(0).getDescription();
            log.info("OCR 추출 완료. 텍스트 길이: {} 자", fullText.length());

            return fullText;

        } catch (IOException e) {
            log.error("Google Vision Client 생성 실패", e);
            throw new CustomException(OcrErrorCode.VISION_CREDENTIALS_ERROR);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google Vision API 호출 중 예외 발생", e);
            throw new CustomException(OcrErrorCode.VISION_API_ERROR);
        }
    }
}