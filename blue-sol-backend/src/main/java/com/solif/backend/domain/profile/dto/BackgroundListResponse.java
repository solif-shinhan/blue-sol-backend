package com.solif.backend.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class BackgroundListResponse {

    @Schema(description = "DB 저장용 배경 패턴명/경로", example = "backgrounds/pattern1.png")
    private String backgroundPattern;

    @Schema(description = "프론트 표시용 전체 이미지 URL", example = "https://bucket.s3.region.amazonaws.com/backgrounds/pattern1.png")
    private String backgroundImageUrl;
}
