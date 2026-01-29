package com.solif.backend.domain.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "댓글 작성 요청")
public class CommentCreateRequest {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Schema(description = "댓글 내용", example = "도움이 되는 글이네요!")
    private String commentContent;

    @NotNull(message = "익명 여부는 필수입니다.")
    @Schema(description = "익명 여부", example = "false")
    private Boolean commentIsAnonymous;
}