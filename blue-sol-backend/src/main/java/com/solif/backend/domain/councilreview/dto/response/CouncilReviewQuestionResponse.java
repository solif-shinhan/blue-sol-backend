package com.solif.backend.domain.councilreview.dto.response;

import com.solif.backend.domain.councilreview.entity.CouncilReviewQuestion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "자치회 활동 후기 질문 응답")
public class CouncilReviewQuestionResponse {

    @Schema(description = "질문 ID", example = "5")
    private Long questionId;

    @Schema(description = "질문 내용", example = "오늘 먹은 음식은요?")
    private String questionText;

    public static CouncilReviewQuestionResponse from(CouncilReviewQuestion question) {
        return CouncilReviewQuestionResponse.builder()
                .questionId(question.getCouncilReviewQuestionId())
                .questionText(question.getQuestionText())
                .build();
    }
}