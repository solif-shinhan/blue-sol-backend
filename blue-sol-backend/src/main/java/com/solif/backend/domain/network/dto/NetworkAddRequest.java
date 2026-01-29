package com.solif.backend.domain.network.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
}
