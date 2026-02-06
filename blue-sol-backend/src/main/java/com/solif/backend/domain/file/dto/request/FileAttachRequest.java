package com.solif.backend.domain.file.dto.request;

import com.solif.backend.domain.file.entity.AttachmentPurpose;
import com.solif.backend.domain.file.entity.FileTargetType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FileAttachRequest {

    @NotNull(message = "파일 ID는 필수입니다")
    private Long fileId;

    @NotNull(message = "타겟 타입은 필수입니다")
    private FileTargetType fileTargetType;

    @NotNull(message = "타겟 ID는 필수입니다")
    private Long targetId;

    @NotNull(message = "용도는 필수입니다")
    private AttachmentPurpose purpose;

    private Integer sortOrder;
}
