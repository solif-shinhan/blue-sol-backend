package com.solif.backend.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class CharacterListResponse {

    @Schema(description = "DB 저장용 캐릭터 패턴명/경로", example = "characters/character_1.svg")
    private String characterPattern;

    @Schema(description = "프론트 표시용 캐릭터 전체 이미지 URL", example = "https://bucket.s3.region.amazonaws.com/characters/character_1.svg")
    private String characterImageUrl;
}
