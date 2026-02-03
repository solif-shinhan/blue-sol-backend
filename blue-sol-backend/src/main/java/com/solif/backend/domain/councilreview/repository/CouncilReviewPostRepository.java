package com.solif.backend.domain.councilreview.repository;

import com.solif.backend.domain.councilreview.entity.CouncilReviewPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouncilReviewPostRepository extends JpaRepository<CouncilReviewPost, Long> {

    // 자치회별 활동 후기 목록 조회 (삭제되지 않은 것만, Post와 Council Fetch Join)
    @Query("SELECT crp FROM CouncilReviewPost crp " +
            "JOIN FETCH crp.post p " +
            "JOIN FETCH p.author " +
            "JOIN FETCH crp.council c " +
            "WHERE crp.council.councilId = :councilId " +
            "AND crp.deletedAt IS NULL " +
            "ORDER BY crp.createdAt DESC")
    Slice<CouncilReviewPost> findByCouncilIdWithPostAndCouncil(
            @Param("councilId") Long councilId,
            Pageable pageable
    );

    // 활동 후기 상세 조회 (Post, Council Fetch Join)
    @Query("SELECT crp FROM CouncilReviewPost crp " +
            "JOIN FETCH crp.post p " +
            "JOIN FETCH p.author " +
            "JOIN FETCH crp.council c " +
            "WHERE crp.councilReviewPostId = :postId " +
            "AND crp.deletedAt IS NULL")
    Optional<CouncilReviewPost> findByIdWithPostAndCouncil(@Param("postId") Long postId);

    // Post ID로 조회
    @Query("SELECT crp FROM CouncilReviewPost crp " +
            "WHERE crp.post.postId = :postId")
    Optional<CouncilReviewPost> findByPostId(@Param("postId") Long postId);
}