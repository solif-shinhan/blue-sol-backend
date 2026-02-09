package com.solif.backend.domain.mentoring.dto;

import com.solif.backend.domain.mentoring.entity.MentoringRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "멘토링 신청 응답")
public class MentoringRequestCreateResponse {

    @Schema(description = "멘토링 신청 ID", example = "1")
    private Long mentoringRequestId;

    @Schema(description = "멘토 이름", example = "신석균")
    private String mentorName;

    @Schema(description = "신청 상태", example = "PENDING")
    private String status;

    @Schema(description = "신청 일시", example = "2026-02-09T12:00:00")
    private LocalDateTime createdAt;

    public static MentoringRequestCreateResponse from(MentoringRequest request) {
        return MentoringRequestCreateResponse.builder()
                .mentoringRequestId(request.getMentoringRequestId())
                .mentorName(request.getMentor().getMentorName())
                .status(request.getStatus().name())
                .createdAt(request.getCreatedAt())
                .build();
    }
}