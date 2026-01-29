package com.solif.backend.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "프로필 수정 응답")
public class ProfileUpdateResponse {

    @Schema(description = "프로필 ID", example = "1")
    private Long profileId;

    public static ProfileUpdateResponse of(Long profileId) {
        return ProfileUpdateResponse.builder()
                .profileId(profileId)
                .build();
    }
}
