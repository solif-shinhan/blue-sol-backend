package com.solif.backend.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "프로필 생성 응답")
public class ProfileCreateResponse {

    @Schema(description = "프로필 ID", example = "1")
    private Long profileId;

    @Schema(description = "QR 코드 이미지 URL")
    private String qrCodeUrl;

    public static ProfileCreateResponse of(Long profileId, String qrCodeUrl) {
        return ProfileCreateResponse.builder()
                .profileId(profileId)
                .qrCodeUrl(qrCodeUrl)
                .build();
    }
}
