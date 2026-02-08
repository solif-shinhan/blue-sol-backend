package com.solif.backend.domain.post.dto;

import com.solif.backend.domain.post.entity.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "게시글 작성 요청")
public class PostCreateRequest {

    @NotNull(message = "게시판 ID는 필수입니다.")
    @Schema(description = "게시판 ID (1: 활동후기, 2: 고민상담, 3: 재단소식)", example = "2")
    private Long boardId;

    @NotNull(message = "카테고리는 필수입니다.")
    @Schema(description = "카테고리", example = "STUDY")
    private PostCategory postCategory;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
    @Schema(description = "게시글 제목", example = "부모님과 고등학교 문제로 다투었어요")
    private String postTitle;

    @NotBlank(message = "내용은 필수입니다.")
    @Schema(description = "게시글 내용")
    private String postContent;

    @Schema(description = "멘토링 요청 ID (멘토링 후기 작성 시)", nullable = true)
    private Long mentoringRequestId;

    @Schema(description = "첨부 파일 ID 목록", nullable = true)
    private List<Long> fileIds;
}