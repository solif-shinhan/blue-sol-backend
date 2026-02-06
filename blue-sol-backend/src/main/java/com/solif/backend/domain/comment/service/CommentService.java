package com.solif.backend.domain.comment.service;

import com.solif.backend.domain.auth.code.AuthErrorCode;
import com.solif.backend.domain.comment.code.CommentErrorCode;
import com.solif.backend.domain.comment.dto.CommentCreateRequest;
import com.solif.backend.domain.comment.dto.CommentResponse;
import com.solif.backend.domain.comment.dto.CommentUpdateRequest;
import com.solif.backend.domain.comment.entity.Comment;
import com.solif.backend.domain.comment.repository.CommentRepository;
import com.solif.backend.domain.post.code.PostErrorCode;
import com.solif.backend.domain.post.entity.Post;
import com.solif.backend.domain.post.repository.PostRepository;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 댓글 목록 조회
    public List<CommentResponse> getComments(Long postId) {
        log.info("댓글 목록 조회 - postId: {}", postId);

        // 게시글 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

        // 삭제된 게시글 체크
        if (post.isDeleted()) {
            throw new CustomException(PostErrorCode.DELETED_POST);
        }

        // 댓글 조회
        List<Comment> comments = commentRepository.findByPost_PostIdOrderByCreatedAtAsc(postId);

        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }

    // 댓글 작성
    @Transactional
    public CommentResponse createComment(Long userId, Long postId, CommentCreateRequest request) {
        log.info("댓글 작성 - userId: {}, postId: {}", userId, postId);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

        // 삭제된 게시글 체크
        if (post.isDeleted()) {
            throw new CustomException(PostErrorCode.DELETED_POST);
        }

        // Comment 엔티티 생성
        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .commentContent(request.getCommentContent())
                .commentIsAnonymous(request.getCommentIsAnonymous())
                .build();

        // 저장
        Comment savedComment = commentRepository.save(comment);

        return CommentResponse.from(savedComment);
    }

    // 댓글 수정
    @Transactional
    public void updateComment(Long userId, Long commentId, CommentUpdateRequest request) {
        log.info("댓글 수정 - userId: {}, commentId: {}", userId, commentId);

        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

        // 작성자 본인 확인
        if (!comment.isAuthor(userId)) {
            throw new CustomException(CommentErrorCode.UNAUTHORIZED_COMMENT_ACCESS);
        }

        // 댓글 수정
        comment.updateContent(request.getCommentContent());
    }

    // 댓글 삭제 (Hard Delete)
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        log.info("댓글 삭제 - userId: {}, commentId: {}", userId, commentId);

        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

        // 작성자 본인 확인
        if (!comment.isAuthor(userId)) {
            throw new CustomException(CommentErrorCode.UNAUTHORIZED_COMMENT_ACCESS);
        }

        // Hard Delete
        commentRepository.delete(comment);
    }
}