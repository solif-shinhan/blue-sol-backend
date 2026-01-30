package com.solif.backend.domain.post.controller;

import com.solif.backend.domain.post.dto.*;
import com.solif.backend.domain.post.code.PostSuccessCode;
import com.solif.backend.domain.post.entity.PostCategory;
import com.solif.backend.domain.post.service.PostService;
import com.solif.backend.global.common.response.ResponseFactory;
import com.solif.backend.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "게시글", description = "게시글 API")
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 목록 조회", description = "게시판별 게시글 목록을 페이징하여 조회합니다. 카테고리 필터링 가능합니다.")
    @GetMapping
    public ResponseEntity<SuccessResponse<Slice<PostListResponse>>> getPosts(
            @RequestParam Long boardId,
            @RequestParam(required = false) PostCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Slice<PostListResponse> posts = postService.getPosts(boardId, category, pageable);
        return ResponseFactory.success(PostSuccessCode.POST_LIST_SUCCESS, posts);
    }

    @Operation(
            summary = "게시글 상세 조회",
            description = "게시글 상세 내용을 조회합니다. 조회수가 1 증가합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{postId}")
    public ResponseEntity<SuccessResponse<PostDetailResponse>> getPostDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {
        PostDetailResponse post = postService.getPostDetail(userId, postId);
        return ResponseFactory.success(PostSuccessCode.POST_DETAIL_SUCCESS, post);
    }

    @Operation(
            summary = "게시글 작성",
            description = "새로운 게시글을 작성합니다. 모든 게시판에서 카테고리가 필수입니다. (자치회 활동 후기는 별도 API)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<SuccessResponse<PostCreateResponse>> createPost(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PostCreateRequest request
    ) {
        PostCreateResponse response = postService.createPost(userId, request);
        return ResponseFactory.success(PostSuccessCode.POST_CREATE_SUCCESS, response);
    }

    @Operation(
            summary = "게시글 수정",
            description = "게시글의 제목과 내용을 수정합니다. 작성자만 수정 가능합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PatchMapping("/{postId}")
    public ResponseEntity<SuccessResponse<Void>> updatePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        postService.updatePost(userId, postId, request);
        return ResponseFactory.success(PostSuccessCode.POST_UPDATE_SUCCESS);
    }

    @Operation(
            summary = "게시글 삭제",
            description = "게시글을 삭제합니다. 작성자만 삭제 가능합니다. Soft Delete로 처리됩니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{postId}")
    public ResponseEntity<SuccessResponse<Void>> deletePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {
        postService.deletePost(userId, postId);
        return ResponseFactory.success(PostSuccessCode.POST_DELETE_SUCCESS);
    }
}