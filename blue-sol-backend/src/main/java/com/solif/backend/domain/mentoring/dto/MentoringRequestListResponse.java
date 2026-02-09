package com.solif.backend.domain.mentoring.dto;

import com.solif.backend.domain.mentoring.entity.MentoringRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "멘토링 신청 내역 응답")
public class MentoringRequestListResponse {

    @Schema(description = "멘토링 신청 ID", example = "1")
    private Long mentoringRequestId;

    @Schema(description = "멘토 이름", example = "신석균")
    private String mentorName;

    @Schema(description = "멘토 직함", example = "멘토 SO&L 글로벌자산운용 대표")
    private String mentorTitle;

    @Schema(description = "멘토 프로필 이미지 URL")
    private String mentorProfileImageUrl;

    @Schema(description = "카테고리", example = "STUDY")
    private String category;

    @Schema(description = "멘토링 방식", example = "MESSAGE")
    private String method;

    @Schema(description = "신청 상태", example = "PENDING")
    private String status;

    @Schema(description = "신청 내용", example = "안녕하세요. 자산관리에 대해 멘토링을 받고 싶습니다.")
    private String content;

    // 관리자 답변
    @Schema(description = "관리자 답변", example = "김철수님, 멘토링 신청 감사합니다...", nullable = true)
    private String adminReply;

    @Schema(description = "신청 일시", example = "2026-02-09T12:00:00")
    private LocalDateTime createdAt;

    // 답변 일시
    @Schema(description = "답변 일시", example = "2026-02-10T15:00:00", nullable = true)
    private LocalDateTime repliedAt;

    public static MentoringRequestListResponse from(MentoringRequest request, String mentorProfileImageUrl) {
        return MentoringRequestListResponse.builder()
                .mentoringRequestId(request.getMentoringRequestId())
                .mentorName(request.getMentor().getMentorName())
                .mentorTitle(request.getMentor().getMentorTitle())
                .mentorProfileImageUrl(mentorProfileImageUrl)
                .category(request.getCategory().name())
                .method(request.getMethod().name())
                .status(request.getStatus().name())
                .content(request.getContent())
                .adminReply(request.getAdminReply())
                .createdAt(request.getCreatedAt())
                .repliedAt(request.getRepliedAt())
                .build();
    }
}