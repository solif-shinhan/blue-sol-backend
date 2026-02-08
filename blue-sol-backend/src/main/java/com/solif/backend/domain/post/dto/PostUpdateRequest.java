package com.solif.backend.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "게시글 수정 요청")
public class PostUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
    @Schema(description = "게시글 제목", example = "수정된 제목입니다")
    private String postTitle;

    @NotBlank(message = "내용은 필수입니다.")
    @Schema(description = "게시글 내용")
    private String postContent;

    @Schema(description = "첨부 파일 ID 목록 (null=변경없음, []=전체삭제, [1,2,3]=교체)", nullable = true)
    private List<Long> fileIds;
}