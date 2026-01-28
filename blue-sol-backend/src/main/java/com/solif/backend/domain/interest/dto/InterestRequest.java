package com.solif.backend.domain.interest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "관심사 등록 요청")
public class InterestRequest {

    @NotEmpty(message = "관심사를 선택해주세요.")
    @Size(min = 2, message = "관심사는 최소 2개 이상 선택해야 합니다.")
    @Schema(description = "관심사 카테고리 목록", example = "[\"봉사활동\", \"독서\", \"금융\"]")
    private List<String> categoryNames;
}
