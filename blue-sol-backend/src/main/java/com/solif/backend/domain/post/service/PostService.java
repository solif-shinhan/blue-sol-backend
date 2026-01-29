package com.solif.backend.domain.post.service;

import com.solif.backend.domain.auth.exception.AuthErrorCode;
import com.solif.backend.domain.board.code.BoardErrorCode;
import com.solif.backend.domain.board.entity.Board;
import com.solif.backend.domain.board.repository.BoardRepository;
import com.solif.backend.domain.comment.dto.PostCommentCount;
import com.solif.backend.domain.comment.repository.CommentRepository;
import com.solif.backend.domain.post.dto.*;
import com.solif.backend.domain.post.entity.Post;
import com.solif.backend.domain.post.entity.PostCategory;
import com.solif.backend.domain.post.code.PostErrorCode;
import com.solif.backend.domain.post.repository.PostRepository;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    // 게시글 목록 조회
    public Slice<PostListResponse> getPosts(Long boardId, PostCategory category, Pageable pageable) {
        log.info("게시글 목록 조회 - boardId: {}, category: {}", boardId, category);

        // Board 존재 확인
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(BoardErrorCode.BOARD_NOT_FOUND));

        // 익명 여부 판단 (boardId=1: 실명, boardId=2: 익명, boardId=3: 실명)
        boolean isAnonymous = (boardId == 2L);

        // 카테고리 있으면 카테고리별 조회, 없으면 전체 조회
        Slice<Post> posts;

        // boardId=1 (활동 후기) - 실명, 멘토링 후기만 조회 (자치회 제외)
        if (boardId == 1L) {
            if (category != null) {
                posts = postRepository.findByBoard_BoardIdAndPostCategoryAndDeletedAtIsNullWithAuthor(boardId, category, pageable);
            } else {
                posts = postRepository.findMentoringPostsByBoardIdWithAuthor(boardId, pageable);
            }
        }
        // boardId=2 (고민상담) - 익명, Author fetch 불필요
        else if (boardId == 2L) {
            if (category != null) {
                posts = postRepository.findByBoard_BoardIdAndPostCategoryAndDeletedAtIsNull(boardId, category, pageable);
            } else {
                posts = postRepository.findByBoard_BoardIdAndDeletedAtIsNull(boardId, pageable);
            }
        }
        // boardId=3 (재단소식) - 실명
        else {
            if (category != null) {
                posts = postRepository.findByBoard_BoardIdAndPostCategoryAndDeletedAtIsNullWithAuthor(boardId, category, pageable);
            } else {
                posts = postRepository.findByBoard_BoardIdAndDeletedAtIsNullWithAuthor(boardId, pageable);
            }
        }

        // N+1 해결: 모든 postId를 모아서 한 번에 댓글 수 조회
        List<Long> postIds = posts.getContent()
                .stream()
                .map(Post::getPostId)
                .toList();

        // 빈 리스트 처리: postIds가 비어있으면 빈 Map 반환
        Map<Long, Long> commentCounts = postIds.isEmpty()
                ? Map.of()
                : commentRepository.countByPostIds(postIds)
                .stream()
                .collect(Collectors.toMap(
                        PostCommentCount::getPostId,
                        PostCommentCount::getCommentCount
                ));

        // Post -> PostListResponse 변환
        return posts.map(post -> {
            Long commentCount = commentCounts.getOrDefault(post.getPostId(), 0L);
            return PostListResponse.from(post, commentCount, isAnonymous);
        });
    }

    // 게시글 상세 조회
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
        Long commentCount = commentRepository.countByPost_PostId(postId);

        return PostDetailResponse.from(post, commentCount, isAnonymous);
    }

    // 게시글 작성
    @Transactional
    public PostCreateResponse createPost(Long userId, PostCreateRequest request) {
        log.info("게시글 작성 - userId: {}, boardId: {}", userId, request.getBoardId());

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 게시판 조회
        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(() -> new CustomException(BoardErrorCode.BOARD_NOT_FOUND));

        // 멘토링 후기(1), 고민상담(2), 재단소식(3) 모두 카테고리 필수
        if (request.getPostCategory() == null) {
            throw new CustomException(PostErrorCode.CATEGORY_REQUIRED);
        }

        // Post 엔티티 생성
        Post post = Post.builder()
                .author(user)
                .board(board)
                .postCategory(request.getPostCategory())
                .postTitle(request.getPostTitle())
                .postContent(request.getPostContent())
                .build();

        // 저장
        Post savedPost = postRepository.save(post);

        // TODO: mentoringRequestId 처리 (나중에 멘토링 도메인 구현 시)
        // if (request.getMentoringRequestId() != null) {
        //     mentoringRequestRepository.updateReviewPostId(
        //         request.getMentoringRequestId(), savedPost.getPostId());
        // }

        return PostCreateResponse.from(savedPost);
    }

    // 게시글 수정
    @Transactional
    public void updatePost(Long userId, Long postId, PostUpdateRequest request) {
        log.info("게시글 수정 - userId: {}, postId: {}", userId, postId);

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

        // 삭제된 게시글 체크
        if (post.isDeleted()) {
            throw new CustomException(PostErrorCode.DELETED_POST);
        }

        // 작성자 본인 확인
        if (!post.isAuthor(userId)) {
            throw new CustomException(PostErrorCode.UNAUTHORIZED_POST_ACCESS);
        }

        // 게시글 수정
        post.updatePost(request.getPostTitle(), request.getPostContent());
    }

    // 게시글 삭제 (Soft Delete)
    @Transactional
    public void deletePost(Long userId, Long postId) {
        log.info("게시글 삭제 - userId: {}, postId: {}", userId, postId);

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

        // 이미 삭제된 게시글 체크
        if (post.isDeleted()) {
            throw new CustomException(PostErrorCode.DELETED_POST);
        }

        // 작성자 본인 확인
        if (!post.isAuthor(userId)) {
            throw new CustomException(PostErrorCode.UNAUTHORIZED_POST_ACCESS);
        }

        // Soft Delete
        post.softDelete();
    }
}