package com.solif.backend.domain.auth.dto;

import com.solif.backend.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "로그인 응답")
public class LoginResponse {

    @Schema(description = "인증 토큰")
    private String token;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자 역할", example = "JUNIOR")
    private User.UserRole userRole;

    public static LoginResponse of(String token, Long userId, User.UserRole userRole) {
        return LoginResponse.builder()
                .token(token)
                .userId(userId)
                .userRole(userRole)
                .build();
    }
}
