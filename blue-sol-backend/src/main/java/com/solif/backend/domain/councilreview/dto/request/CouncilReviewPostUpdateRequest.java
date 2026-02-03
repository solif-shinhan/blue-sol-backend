package com.solif.backend.domain.councilreview.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "자치회 활동 후기 수정 요청")
public class CouncilReviewPostUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
    @Schema(description = "게시글 제목", example = "우리들의 첫 만남 (수정)")
    private String postTitle;

    @NotNull(message = "활동 날짜는 필수입니다.")
    @Schema(description = "활동 날짜", example = "2026-02-11")
    private LocalDate activityDate;

    @NotBlank(message = "활동 장소는 필수입니다.")
    @Size(max = 255, message = "활동 장소는 255자 이하여야 합니다.")
    @Schema(description = "활동 장소", example = "별마당 도서관 2층")
    private String activityLocation;

    @NotNull(message = "지출 총액은 필수입니다.")
    @Min(value = 0, message = "지출 총액은 0원 이상이어야 합니다.")
    @Schema(description = "지출 총액", example = "150000")
    private Long totalCost;

    @NotEmpty(message = "참여자는 최소 1명 이상이어야 합니다.")
    @Schema(description = "참여자 사용자 ID 목록", example = "[1, 2, 3, 4, 5, 6, 7]")
    private List<Long> participantUserIds;

    @Schema(description = "이미지 파일 ID 목록 (선택)", example = "[10, 11, 12, 13]")
    private List<Long> imageFileIds;
}