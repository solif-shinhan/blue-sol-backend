package com.solif.backend.domain.interest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "관심사 등록 응답")
public class InterestResponse {

    @Schema(description = "등록된 관심사 개수", example = "3")
    private int count;

    @Schema(description = "등록된 관심사 목록", example = "[\"봉사활동\", \"독서\", \"금융\"]")
    private List<String> categoryNames;

    public static InterestResponse of(int count, List<String> categoryNames) {
        return InterestResponse.builder()
                .count(count)
                .categoryNames(categoryNames)
                .build();
    }
}
