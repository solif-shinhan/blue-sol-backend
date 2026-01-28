package com.solif.backend.domain.post.controller;

import com.solif.backend.domain.post.dto.PostDetailResponse;
import com.solif.backend.domain.post.dto.PostListResponse;
import com.solif.backend.domain.post.code.PostSuccessCode;
import com.solif.backend.domain.post.entity.PostCategory;
import com.solif.backend.domain.post.service.PostService;
import com.solif.backend.global.common.response.ResponseFactory;
import com.solif.backend.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 내용을 조회합니다. 조회수가 1 증가합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<SuccessResponse<PostDetailResponse>> getPostDetail(
            @PathVariable Long postId
    ) {
        PostDetailResponse post = postService.getPostDetail(postId);
        return ResponseFactory.success(PostSuccessCode.POST_DETAIL_SUCCESS, post);
    }
}