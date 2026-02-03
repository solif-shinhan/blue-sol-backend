package com.solif.backend.domain.councilreview.repository;

import com.solif.backend.domain.councilreview.entity.CouncilReviewParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CouncilReviewParticipantRepository extends JpaRepository<CouncilReviewParticipant, Long> {

    // 특정 활동 후기의 참여자 목록 조회 (User Fetch Join)
    @Query("SELECT crp FROM CouncilReviewParticipant crp " +
            "JOIN FETCH crp.user u " +
            "WHERE crp.councilReviewPost.councilReviewPostId = :postId " +
            "ORDER BY crp.createdAt ASC")
    List<CouncilReviewParticipant> findByCouncilReviewPostIdWithUser(@Param("postId") Long postId);

    // 특정 활동 후기의 참여자 수 조회
    Long countByCouncilReviewPost_CouncilReviewPostId(Long postId);

    // 특정 사용자가 특정 활동 후기의 참여자인지 확인
    @Query("SELECT COUNT(crp) > 0 FROM CouncilReviewParticipant crp " +
            "WHERE crp.councilReviewPost.councilReviewPostId = :postId " +
            "AND crp.user.userId = :userId")
    boolean existsByCouncilReviewPostIdAndUserId(
            @Param("postId") Long postId,
            @Param("userId") Long userId
    );

    // 특정 활동 후기의 모든 참여자 삭제 (Hard Delete)
    @Modifying
    @Query("DELETE FROM CouncilReviewParticipant crp " +
            "WHERE crp.councilReviewPost.councilReviewPostId = :postId")
    void deleteByCouncilReviewPostId(@Param("postId") Long postId);

    // 특정 활동 후기에서 특정 사용자 제거 (Hard Delete)
    @Modifying
    @Query("DELETE FROM CouncilReviewParticipant crp " +
            "WHERE crp.councilReviewPost.councilReviewPostId = :postId " +
            "AND crp.user.userId = :userId")
    void deleteByCouncilReviewPostIdAndUserId(
            @Param("postId") Long postId,
            @Param("userId") Long userId
    );

    // 특정 활동 후기의 참여자 User ID 목록 조회
    @Query("SELECT crp.user.userId FROM CouncilReviewParticipant crp " +
            "WHERE crp.councilReviewPost.councilReviewPostId = :postId")
    List<Long> findUserIdsByCouncilReviewPostId(@Param("postId") Long postId);
}