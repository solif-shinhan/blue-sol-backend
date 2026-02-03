package com.solif.backend.domain.file.dto.response;

import com.solif.backend.domain.file.entity.File;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResponse {
    private Long fileId;
    private String originalName;
    private String contentType;
    private Long sizeBytes;
    private String url;

    public static FileUploadResponse from(File file, String region) {
        return FileUploadResponse.builder()
                .fileId(file.getFileId())
                .originalName(file.getOriginalName())
                .contentType(file.getContentType())
                .sizeBytes(file.getSizeBytes())
                .url(file.getUrl(region))
                .build();
    }
}
