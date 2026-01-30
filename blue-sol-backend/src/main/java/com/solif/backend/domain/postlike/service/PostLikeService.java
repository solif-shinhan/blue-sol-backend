package com.solif.backend.domain.postlike.service;

import com.solif.backend.domain.auth.exception.AuthErrorCode;
import com.solif.backend.domain.post.code.PostErrorCode;
import com.solif.backend.domain.post.entity.Post;
import com.solif.backend.domain.post.repository.PostRepository;
import com.solif.backend.domain.postlike.code.PostLikeErrorCode;
import com.solif.backend.domain.postlike.entity.PostLike;
import com.solif.backend.domain.postlike.repository.PostLikeRepository;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 좋아요 추가
    @Transactional
    public void likePost(Long userId, Long postId) {
        log.info("좋아요 추가 - userId: {}, postId: {}", userId, postId);

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

        // 이미 좋아요한 경우
        if (postLikeRepository.existsByUser_UserIdAndPost_PostId(userId, postId)) {
            throw new CustomException(PostLikeErrorCode.ALREADY_LIKED);
        }

        // PostLike 엔티티 생성 및 저장
        PostLike postLike = PostLike.builder()
                .user(user)
                .post(post)
                .build();

        postLikeRepository.save(postLike);
    }

    // 좋아요 취소
    @Transactional
    public void unlikePost(Long userId, Long postId) {
        log.info("좋아요 취소 - userId: {}, postId: {}", userId, postId);

        // 좋아요 조회
        PostLike postLike = postLikeRepository.findByUser_UserIdAndPost_PostId(userId, postId)
                .orElseThrow(() -> new CustomException(PostLikeErrorCode.LIKE_NOT_FOUND));

        // 삭제
        postLikeRepository.delete(postLike);
    }
}