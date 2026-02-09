package com.solif.backend.domain.mentoringpost.repository;

import com.solif.backend.domain.mentoringpost.entity.MentoringPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MentoringPostRepository extends JpaRepository<MentoringPost, Long> {

    // Post ID로 조회
    @Query("SELECT mp FROM MentoringPost mp " +
            "WHERE mp.post.postId = :postId")
    Optional<MentoringPost> findByPostId(@Param("postId") Long postId);

    // 삭제되지 않은 게시글 조회 (상세 조회용)
    @Query("SELECT mp FROM MentoringPost mp " +
            "JOIN FETCH mp.post p " +
            "JOIN FETCH p.author " +
            "WHERE mp.mentoringPostId = :mentoringPostId " +
            "AND mp.deletedAt IS NULL")
    Optional<MentoringPost> findActiveByIdWithPost(@Param("mentoringPostId") Long mentoringPostId);

    // 삭제 여부 무관 조회 (수정/삭제용)
    @Query("SELECT mp FROM MentoringPost mp " +
            "JOIN FETCH mp.post p " +
            "JOIN FETCH p.author " +
            "WHERE mp.mentoringPostId = :mentoringPostId")
    Optional<MentoringPost> findByIdWithPost(@Param("mentoringPostId") Long mentoringPostId);
}