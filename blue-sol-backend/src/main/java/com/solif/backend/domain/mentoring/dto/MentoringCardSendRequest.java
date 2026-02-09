package com.solif.backend.domain.mentoring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "멘토링 엽서 발송 요청")
public class MentoringCardSendRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
    @Schema(description = "엽서 제목", example = "금융권 IB 직무 멘토를 찾고 싶습니다")
    private String cardTitle;

    @NotNull(message = "카테고리는 필수입니다.")
    @Schema(description = "멘토링 카테고리 (STUDY/JOB/ADMISSION/ETC)", example = "JOB")
    private String category;

    @NotNull(message = "멘토링 방식은 필수입니다.")
    @Schema(description = "멘토링 방식 (MESSAGE/VIDEO/PHONE/OFFLINE)", example = "VIDEO")
    private String method;

    @NotBlank(message = "내용은 필수입니다.")
    @Schema(description = "엽서 내용", example = "안녕하세요. 저는 한국대학교에 재학중인 학생입니다...")
    private String cardContent;

    @Schema(description = "첨부 파일 ID 목록", nullable = true)
    private List<Long> fileIds;
}