package com.solif.backend.domain.auth.dto;

import com.solif.backend.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "회원가입 응답")
public class SignupResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자 역할", example = "JUNIOR")
    private User.UserRole userRole;

    @Schema(description = "생성 일시", example = "2026-01-26T00:23:11")
    private LocalDateTime createdAt;

    public static SignupResponse of(Long userId, User.UserRole userRole, LocalDateTime createdAt) {
        return SignupResponse.builder()
                .userId(userId)
                .userRole(userRole)
                .createdAt(createdAt)
                .build();
    }
}
