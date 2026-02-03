package com.solif.backend.global.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.solif.backend.domain.file.exception.FileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.List;

import static com.solif.backend.domain.file.exception.FileException.FileErrorCode.FILE_UPLOAD_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final AmazonS3 amazonS3;

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
        }catch (SdkException e){
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

    public List<String> getFileList(String bucket, String prefix) {
        ListObjectsV2Result result = amazonS3.listObjectsV2(bucket, prefix);
        return result.getObjectSummaries().stream()
                .map(S3ObjectSummary::getKey)
                .filter(key -> !key.equals(prefix))
                .toList();
    }
}
