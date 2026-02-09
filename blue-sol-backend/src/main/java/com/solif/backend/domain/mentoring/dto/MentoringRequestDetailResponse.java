package com.solif.backend.domain.mentoring.dto;

import com.solif.backend.domain.mentoring.entity.MentoringRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "멘토링 신청서 상세 응답")
public class MentoringRequestDetailResponse {

    @Schema(description = "멘토링 신청 ID", example = "1")
    private Long mentoringRequestId;

    @Schema(description = "멘토 ID", example = "1")
    private Long mentorId;

    @Schema(description = "멘토 이름", example = "신석균")
    private String mentorName;

    @Schema(description = "멘토 직함", example = "멘토 SO&L 글로벌자산운용 대표")
    private String mentorTitle;

    @Schema(description = "멘토 소개", example = "안녕하세요. 저는 SO&L 글로벌자산운용 대표...")
    private String mentorIntro;

    @Schema(description = "멘토 프로필 이미지 URL", nullable = true)
    private String mentorProfileImageUrl;

    @Schema(description = "신청자 ID", example = "5")
    private Long menteeUserId;

    @Schema(description = "신청자 이름", example = "김철수")
    private String menteeName;

    @Schema(description = "카테고리", example = "STUDY")
    private String category;

    @Schema(description = "멘토링 방식", example = "MESSAGE")
    private String method;

    @Schema(description = "신청 상태", example = "PENDING")
    private String status;

    @Schema(description = "신청 내용", example = "안녕하세요. 자산관리에 대해 멘토링을 받고 싶습니다...")
    private String content;

    @Schema(description = "관리자 답변", nullable = true)
    private String adminReply;

    @Schema(description = "신청 일시", example = "2026-02-09T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "상태 변경 일시", nullable = true)
    private LocalDateTime updatedAt;

    @Schema(description = "답변 일시", nullable = true)
    private LocalDateTime repliedAt;

    public static MentoringRequestDetailResponse from(MentoringRequest request, String mentorProfileImageUrl) {
        return MentoringRequestDetailResponse.builder()
                .mentoringRequestId(request.getMentoringRequestId())
                .mentorId(request.getMentor().getMentorId())
                .mentorName(request.getMentor().getMentorName())
                .mentorTitle(request.getMentor().getMentorTitle())
                .mentorIntro(request.getMentor().getMentorIntro())
                .mentorProfileImageUrl(mentorProfileImageUrl)
                .menteeUserId(request.getMenteeUser().getUserId())
                .menteeName(request.getMenteeUser().getName())
                .category(request.getCategory().name())
                .method(request.getMethod().name())
                .status(request.getStatus().name())
                .content(request.getContent())
                .adminReply(request.getAdminReply())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .repliedAt(request.getRepliedAt())
                .build();
    }
}