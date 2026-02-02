package com.solif.backend.domain.council.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "자치회 멤버 추가 요청")
public class AddMemberRequest {

    @NotEmpty(message = "추가할 사용자 ID 목록은 필수입니다.")
    @Schema(description = "추가할 사용자 ID 목록", example = "[2, 3, 4]")
    private List<Long> userIds;
}