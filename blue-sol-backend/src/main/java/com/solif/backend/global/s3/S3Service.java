package com.solif.backend.global.s3;

import com.solif.backend.domain.file.code.FileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.List;

import static com.solif.backend.domain.file.code.FileException.FileErrorCode.FILE_UPLOAD_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    /**
     * S3에 파일 업로드
     * @param folder 폴더명 (예: "qrcodes")
     * @param fileName 파일명 (예: "user123.png")
     * @param data 파일 바이트 배열
     * @param contentType 파일 타입 (예: "image/png")
     * @return 업로드된 파일의 URL
     */
    public String upload(String folder, String fileName, byte[] data, String contentType) {
        String key = folder + "/" + fileName;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(data));

            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
        } catch (SdkException e) {
            log.error("S3 업로드 실패 - Key: {}, Error: {}", key, e.getMessage());
            throw new FileException(FILE_UPLOAD_FAILED);
        }
    }

    /**
     * S3에서 파일 삭제
     * @param objectKey 삭제할 파일의 key (예: "uploads/uuid.png")
     */
    public void delete(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return;

        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(request);
            log.info("S3 삭제 완료: {}", objectKey);
        } catch (SdkException e) {
            log.error("S3 삭제 실패 - Key: {}, Error: {}", objectKey, e.getMessage());
            throw new FileException(FILE_UPLOAD_FAILED);
        }
    }

    /**
     * S3 특정 prefix 하위 파일 Key 목록 조회
     * @param bucket S3 버킷명
     * @param prefix prefix (예: "characters/")
     */
    public List<String> getFileList(String bucket, String prefix) {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            return response.contents().stream()
                    .map(S3Object::key)
                    .filter(key -> !key.equals(prefix)) // prefix 자체(폴더 표시) 제거
                    .toList();

        } catch (SdkException e) {
            log.error("S3 파일 목록 조회 실패 - bucket: {}, prefix: {}, Error: {}", bucket, prefix, e.getMessage());
            throw new FileException(FILE_UPLOAD_FAILED);
        }
    }
}
