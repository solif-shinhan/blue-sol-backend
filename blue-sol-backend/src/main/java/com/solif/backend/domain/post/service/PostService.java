package com.solif.backend.domain.post.service;

import com.solif.backend.domain.board.entity.Board;
import com.solif.backend.domain.board.repository.BoardRepository;
import com.solif.backend.domain.comment.repository.CommentRepository;
import com.solif.backend.domain.post.dto.PostDetailResponse;
import com.solif.backend.domain.post.dto.PostListResponse;
import com.solif.backend.domain.post.entity.Post;
import com.solif.backend.domain.post.entity.PostCategory;
import com.solif.backend.domain.post.code.PostErrorCode;
import com.solif.backend.domain.post.repository.PostRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    // 게시글 목록 조회
    public Slice<PostListResponse> getPosts(Long boardId, PostCategory category, Pageable pageable) {
        log.info("게시글 목록 조회 - boardId: {}, category: {}", boardId, category);

        // Board 존재 확인
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

        // 카테고리 있으면 카테고리별 조회, 없으면 전체 조회
        Slice<Post> posts;
        if (category != null) {
            posts = postRepository.findByBoard_BoardIdAndPostCategoryAndDeletedAtIsNull(
                    boardId, category, pageable);
        } else {
            posts = postRepository.findByBoard_BoardIdAndDeletedAtIsNull(boardId, pageable);
        }

        // 익명 여부 판단 (boardId가 2면 고민상담 = 익명)
        boolean isAnonymous = boardId == 2L;

        // Post -> PostListResponse 변환
        return posts.map(post -> {
            Integer commentCount = commentRepository.countByPost_PostId(post.getPostId());
            return PostListResponse.from(post, commentCount, isAnonymous);
        });
    }

    // 게시글 상세 조회 (조회수 증가)
    @Transactional
    public PostDetailResponse getPostDetail(Long postId) {
        log.info("게시글 상세 조회 - postId: {}", postId);

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

        // 삭제된 게시글 체크
        if (post.isDeleted()) {
            throw new CustomException(PostErrorCode.DELETED_POST);
        }

        // 조회수 증가
        post.increaseViewCount();

        // 익명 여부 판단
        boolean isAnonymous = post.getBoard().getBoardId() == 2L;

        // 댓글 수 조회
        Integer commentCount = commentRepository.countByPost_PostId(postId);

        return PostDetailResponse.from(post, commentCount, isAnonymous);
    }
}