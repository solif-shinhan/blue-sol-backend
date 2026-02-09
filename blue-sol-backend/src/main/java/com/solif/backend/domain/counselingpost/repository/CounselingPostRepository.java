package com.solif.backend.domain.counselingpost.repository;

import com.solif.backend.domain.counselingpost.entity.CounselingPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CounselingPostRepository extends JpaRepository<CounselingPost, Long> {

    // Post ID로 조회
    @Query("SELECT cp FROM CounselingPost cp " +
            "WHERE cp.post.postId = :postId")
    Optional<CounselingPost> findByPostId(@Param("postId") Long postId);

    // 삭제되지 않은 게시글 조회 (상세 조회용)
    @Query("SELECT cp FROM CounselingPost cp " +
            "JOIN FETCH cp.post p " +
            "WHERE cp.counselingPostId = :counselingPostId " +
            "AND cp.deletedAt IS NULL")
    Optional<CounselingPost> findActiveByIdWithPost(@Param("counselingPostId") Long counselingPostId);

    // 삭제 여부 무관 조회 (수정/삭제용)
    @Query("SELECT cp FROM CounselingPost cp " +
            "JOIN FETCH cp.post p " +
            "WHERE cp.counselingPostId = :counselingPostId")
    Optional<CounselingPost> findByIdWithPost(@Param("counselingPostId") Long counselingPostId);
}