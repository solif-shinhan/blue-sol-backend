package com.solif.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "로그아웃 응답")
public class LogoutResponse {

    @Schema(description = "로그아웃 시각", example = "2026-01-26T01:30:00")
    private LocalDateTime logoutAt;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    public static LogoutResponse of(Long userId, LocalDateTime logoutAt) {
        return LogoutResponse.builder()
                .userId(userId)
                .logoutAt(logoutAt)
                .build();
    }
}
