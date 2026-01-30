package com.solif.backend.domain.network.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "교류망 추가 요청")
public class NetworkAddRequest {

    @Schema(description = "QR 코드 (QR 스캔 시)")
    private String targetQrCode;

    @Schema(description = "대상 사용자 ID (목록에서 선택 시)")
    private Long targetUserId;

    @AssertTrue(message = "QR 코드 또는 대상 사용자 ID 중 하나는 필수입니다.")
    @Schema(hidden = true)
    public boolean isValidRequest() {
        return targetQrCode != null || targetUserId != null;
    }
}
