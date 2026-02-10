package com.solif.backend.domain.file.dto.response;

import com.solif.backend.domain.file.entity.AttachmentPurpose;
import com.solif.backend.domain.file.entity.FileAttachment;
import com.solif.backend.domain.file.entity.FileTargetType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileAttachmentResponse {
    private Long fileAttachmentId;
    private Long fileId;
    private String originalName;
    private String url;
    private FileTargetType fileTargetType;
    private Long targetId;
    private AttachmentPurpose purpose;
    private Integer sortOrder;

    public static FileAttachmentResponse from(FileAttachment attachment, String region) {
        return FileAttachmentResponse.builder()
                .fileAttachmentId(attachment.getFileAttachmentId())
                .fileId(attachment.getFile().getFileId())
                .originalName(attachment.getFile().getOriginalName())
                .url(attachment.getFile().getUrl(region))
                .fileTargetType(attachment.getFileTargetType())
                .targetId(attachment.getFileTargetId())
                .purpose(attachment.getPurpose())
                .sortOrder(attachment.getSortOrder())
                .build();
    }
}
