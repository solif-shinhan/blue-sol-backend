package com.solif.backend.domain.mentoring.dto;

import com.solif.backend.domain.mentoring.entity.Mentor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "전문가 멘토 목록 응답")
public class MentorListResponse {

    @Schema(description = "멘토 ID", example = "1")
    private Long mentorId;

    @Schema(description = "멘토 직함", example = "멘토 SO&L 글로벌자산운용 대표")
    private String mentorTitle;

    @Schema(description = "멘토 이름", example = "신석균")
    private String mentorName;

    @Schema(description = "멘토 소개", example = "안녕하세요. 저는 한국대학교에 재학중인 신석균입니다. 대학생활의 시작...")
    private String mentorIntro;

    @Schema(description = "멘토 카테고리", example = "STUDY")
    private String mentorCategory;

    @Schema(description = "프로필 이미지 URL", nullable = true)
    private String profileImageUrl;

    public static MentorListResponse from(Mentor mentor, String profileImageUrl) {
        return MentorListResponse.builder()
                .mentorId(mentor.getMentorId())
                .mentorTitle(mentor.getMentorTitle())
                .mentorName(mentor.getMentorName())
                .mentorIntro(mentor.getMentorIntro())
                .mentorCategory(mentor.getMentorCategory().name())
                .profileImageUrl(profileImageUrl)
                .build();
    }
}