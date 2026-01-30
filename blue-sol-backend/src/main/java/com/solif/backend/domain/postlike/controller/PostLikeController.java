package com.solif.backend.domain.postlike.controller;

import com.solif.backend.domain.postlike.code.PostLikeSuccessCode;
import com.solif.backend.domain.postlike.service.PostLikeService;
import com.solif.backend.global.common.response.ResponseFactory;
import com.solif.backend.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "게시글 좋아요", description = "게시글 좋아요 API")
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @Operation(
            summary = "좋아요 추가",
            description = "게시글에 좋아요를 추가합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{postId}/like")
    public ResponseEntity<SuccessResponse<Void>> likePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {
        postLikeService.likePost(userId, postId);
        return ResponseFactory.success(PostLikeSuccessCode.LIKE_SUCCESS);
    }

    @Operation(
            summary = "좋아요 취소",
            description = "게시글 좋아요를 취소합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{postId}/like")
    public ResponseEntity<SuccessResponse<Void>> unlikePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {
        postLikeService.unlikePost(userId, postId);
        return ResponseFactory.success(PostLikeSuccessCode.UNLIKE_SUCCESS);
    }
}