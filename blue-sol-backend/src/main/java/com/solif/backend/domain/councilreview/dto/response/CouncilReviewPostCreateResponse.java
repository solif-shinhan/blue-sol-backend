package com.solif.backend.domain.councilreview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "자치회 활동 후기 생성 응답")
public class CouncilReviewPostCreateResponse {

    @Schema(description = "활동 후기 게시글 ID", example = "1")
    private Long councilReviewPostId;

    @Schema(description = "통합 게시글 ID (Post)", example = "100")
    private Long postId;

    public static CouncilReviewPostCreateResponse of(Long councilReviewPostId, Long postId) {
        return CouncilReviewPostCreateResponse.builder()
                .councilReviewPostId(councilReviewPostId)
                .postId(postId)
                .build();
    }
}