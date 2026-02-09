package com.solif.backend.domain.mentoring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "멘토링 신청 요청")
public class MentoringRequestCreateRequest {

    @NotNull(message = "멘토 ID는 필수입니다.")
    @Schema(description = "멘토 ID", example = "1")
    private Long mentorId;

    @NotNull(message = "카테고리는 필수입니다.")
    @Schema(description = "멘토링 카테고리 (STUDY/JOB/ADMISSION/ETC)", example = "STUDY")
    private String category;

    @NotBlank(message = "신청 내용은 필수입니다.")
    @Schema(description = "멘토링 신청 내용", example = "안녕하세요. 저는 한국대학교에 재학중인 학생입니다...")
    private String content;

    @NotNull(message = "멘토링 방식은 필수입니다.")
    @Schema(description = "멘토링 방식 (MESSAGE/VIDEO/PHONE/OFFLINE)", example = "MESSAGE")
    private String method;
}