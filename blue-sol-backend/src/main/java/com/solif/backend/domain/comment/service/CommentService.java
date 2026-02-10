package com.solif.backend.domain.comment.service;

import com.solif.backend.domain.auth.code.AuthErrorCode;
import com.solif.backend.domain.comment.code.CommentErrorCode;
import com.solif.backend.domain.comment.dto.CommentCreateRequest;
import com.solif.backend.domain.comment.dto.CommentResponse;
import com.solif.backend.domain.comment.dto.CommentUpdateRequest;
import com.solif.backend.domain.comment.entity.Comment;
import com.solif.backend.domain.comment.repository.CommentRepository;
import com.solif.backend.domain.council.repository.CouncilMemberRepository;
import com.solif.backend.domain.councilreview.entity.CouncilReviewPost;
import com.solif.backend.domain.councilreview.repository.CouncilReviewPostRepository;
import com.solif.backend.domain.mission.entity.MissionConditionType;
import com.solif.backend.domain.mission.service.MissionService;
import com.solif.backend.domain.notification.entity.NotificationType;
import com.solif.backend.domain.notification.entity.NotificationTargetType;
import com.solif.backend.domain.notification.event.NotificationEvent;
import com.solif.backend.domain.post.code.PostErrorCode;
import com.solif.backend.domain.post.entity.Post;
import com.solif.backend.domain.post.repository.PostRepository;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;
    private final CouncilMemberRepository councilMemberRepository;
    private final CouncilReviewPostRepository councilReviewPostRepository;
    private final MissionService missionService;

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

        // 미션 체크: 타인의 게시글에 댓글 작성 (post_id를 sourceId로 전달)
        if (!post.getAuthor().getUserId().equals(userId)) {
            missionService.checkAndCompleteMission(userId, MissionConditionType.COMMENT_CREATE, postId);
        }

        // 알림 발송
        publishCommentNotification(userId, post, savedComment);

        return CommentResponse.from(savedComment);
    }

    /**
     * 댓글 알림 발송 (boardId 기반 분기)
     * - boardId=1 (자치회 활동 후기): COUNCIL_COMMENT → 팀원 전체 (작성자 제외)
     * - boardId=2 (멘토링 후기): MENTORING_REVIEW_COMMENT → 글 작성자
     * - boardId=3,4 (고민상담, 재단소식 등): COMMENT → 글 작성자
     */
    private void publishCommentNotification(Long commenterId, Post post, Comment comment) {
        Long boardId = post.getBoard().getBoardId();
        Long postAuthorId = post.getAuthor().getUserId();
        String commentPreview = truncate(comment.getCommentContent(), 30);

        if (boardId == 1L) {
            // 자치회 활동 후기 → 팀원 전체에게 COUNCIL_COMMENT
            CouncilReviewPost reviewPost = councilReviewPostRepository.findByPostId(post.getPostId()).orElse(null);
            if (reviewPost != null) {
                Long councilId = reviewPost.getCouncil().getCouncilId();
                List<Long> memberUserIds = councilMemberRepository.findUserIdsByCouncilId(councilId);

                for (Long memberId : memberUserIds) {
                    if (!memberId.equals(commenterId)) {
                        eventPublisher.publishEvent(new NotificationEvent(
                                memberId,
                                NotificationType.COUNCIL_COMMENT,
                                NotificationTargetType.COUNCIL_POST,
                                reviewPost.getCouncilReviewPostId(),
                                "우리 팀 활동에 새 댓글이 달렸어요",
                                commentPreview
                        ));
                    }
                }
            }
        } else if (boardId == 2L) {
            // 멘토링 후기 → 글 작성자에게 MENTORING_REVIEW_COMMENT
            if (!commenterId.equals(postAuthorId)) {
                eventPublisher.publishEvent(new NotificationEvent(
                        postAuthorId,
                        NotificationType.MENTORING_REVIEW_COMMENT,
                        NotificationTargetType.MENTORING_REVIEW,
                        post.getPostId(),
                        "작성하신 멘토링 후기에 댓글이 달렸어요.",
                        commentPreview
                ));
            }
        } else {
            // 일반 게시판 (고민상담, 재단소식 등) → 글 작성자에게 COMMENT
            if (!commenterId.equals(postAuthorId)) {
                eventPublisher.publishEvent(new NotificationEvent(
                        postAuthorId,
                        NotificationType.COMMENT,
                        NotificationTargetType.POST,
                        post.getPostId(),
                        "회원님의 글에 새로운 댓글이 달렸어요.",
                        commentPreview
                ));
            }
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
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