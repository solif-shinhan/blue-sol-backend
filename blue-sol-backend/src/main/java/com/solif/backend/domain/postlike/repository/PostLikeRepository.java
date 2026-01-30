package com.solif.backend.domain.postlike.repository;

import com.solif.backend.domain.postlike.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // 좋아요 존재 여부 확인
    boolean existsByUser_UserIdAndPost_PostId(Long userId, Long postId);

    // 좋아요 조회 (삭제용)
    Optional<PostLike> findByUser_UserIdAndPost_PostId(Long userId, Long postId);

    // 게시글의 좋아요 수
    Long countByPost_PostId(Long postId);
}