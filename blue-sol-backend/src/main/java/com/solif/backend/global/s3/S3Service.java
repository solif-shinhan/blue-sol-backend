package com.solif.backend.global.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

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

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(data));

        String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
        log.info("S3 업로드 완료: {}", url);

        return url;
    }
}
