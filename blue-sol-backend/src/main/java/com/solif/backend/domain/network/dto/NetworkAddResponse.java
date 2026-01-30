package com.solif.backend.domain.network.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "교류망 추가 응답")
public class NetworkAddResponse {

    @Schema(description = "연결 ID")
    private Long connectionId;

    @Schema(description = "대상 사용자 ID")
    private Long targetUserId;

    @Schema(description = "대상 사용자 이름")
    private String targetUserName;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;
}
