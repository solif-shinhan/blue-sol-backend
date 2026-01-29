package com.solif.backend.domain.comment.controller;

import com.solif.backend.domain.comment.code.CommentSuccessCode;
import com.solif.backend.domain.comment.dto.CommentCreateRequest;
import com.solif.backend.domain.comment.dto.CommentResponse;
import com.solif.backend.domain.comment.dto.CommentUpdateRequest;
import com.solif.backend.domain.comment.service.CommentService;
import com.solif.backend.global.common.response.ResponseFactory;
import com.solif.backend.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "댓글", description = "댓글 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 댓글 목록을 조회합니다.")
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<SuccessResponse<List<CommentResponse>>> getComments(
            @PathVariable Long postId
    ) {
        List<CommentResponse> comments = commentService.getComments(postId);
        return ResponseFactory.success(CommentSuccessCode.COMMENT_LIST_SUCCESS, comments);
    }

    @Operation(
            summary = "댓글 작성",
            description = "게시글에 댓글을 작성합니다. 익명 여부를 선택할 수 있습니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<SuccessResponse<CommentResponse>> createComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        CommentResponse response = commentService.createComment(userId, postId, request);
        return ResponseFactory.success(CommentSuccessCode.COMMENT_CREATE_SUCCESS, response);
    }

    @Operation(
            summary = "댓글 수정",
            description = "댓글 내용을 수정합니다. 작성자만 수정 가능합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<SuccessResponse<Void>> updateComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        commentService.updateComment(userId, commentId, request);
        return ResponseFactory.success(CommentSuccessCode.COMMENT_UPDATE_SUCCESS);
    }

    @Operation(
            summary = "댓글 삭제",
            description = "댓글을 삭제합니다. 작성자만 삭제 가능합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<SuccessResponse<Void>> deleteComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(userId, commentId);
        return ResponseFactory.success(CommentSuccessCode.COMMENT_DELETE_SUCCESS);
    }
}