package com.solif.backend.domain.councilreview.repository;

import com.solif.backend.domain.councilreview.entity.CouncilReviewRelay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface CouncilReviewRelayRepository extends JpaRepository<CouncilReviewRelay, Long> {

    // 특정 활동 후기의 모든 릴레이 조회 (Writer, Question Fetch Join)
    @Query("SELECT crr FROM CouncilReviewRelay crr " +
            "JOIN FETCH crr.writerUser u " +
            "JOIN FETCH crr.councilReviewQuestion q " +
            "WHERE crr.councilReviewPost.councilReviewPostId = :postId " +
            "ORDER BY crr.relayOrder ASC")
    List<CouncilReviewRelay> findByCouncilReviewPostIdWithWriterAndQuestion(
            @Param("postId") Long postId
    );

    // 릴레이 상세 조회 (Writer, Post, Question Fetch Join)
    @Query("SELECT crr FROM CouncilReviewRelay crr " +
            "JOIN FETCH crr.writerUser u " +
            "JOIN FETCH crr.councilReviewPost crp " +
            "JOIN FETCH crr.councilReviewQuestion q " +
            "WHERE crr.councilReviewRelayId = :relayId")
    Optional<CouncilReviewRelay> findByIdWithWriterAndPost(@Param("relayId") Long relayId);

    // 특정 활동 후기의 릴레이 개수 조회
    Long countByCouncilReviewPost_CouncilReviewPostId(Long postId);

    // 특정 사용자가 특정 활동 후기에 이미 릴레이를 작성했는지 확인
    @Query("SELECT COUNT(crr) > 0 FROM CouncilReviewRelay crr " +
            "WHERE crr.councilReviewPost.councilReviewPostId = :postId " +
            "AND crr.writerUser.userId = :userId")
    boolean existsByCouncilReviewPostIdAndWriterUserId(
            @Param("postId") Long postId,
            @Param("userId") Long userId
    );

    // 특정 활동 후기에서 사용된 질문 ID 목록 조회
    @Query("SELECT crr.councilReviewQuestion.councilReviewQuestionId " +
            "FROM CouncilReviewRelay crr " +
            "WHERE crr.councilReviewPost.councilReviewPostId = :postId")
    List<Long> findUsedQuestionIdsByPostId(@Param("postId") Long postId);

    // 특정 활동 후기의 최대 relay_order 조회
    @Query("SELECT COALESCE(MAX(crr.relayOrder), 0) " +
            "FROM CouncilReviewRelay crr " +
            "WHERE crr.councilReviewPost.councilReviewPostId = :postId")
    Integer findMaxRelayOrderByPostId(@Param("postId") Long postId);

    // 특정 활동 후기의 모든 릴레이 삭제 (Hard Delete)
    @Modifying
    @Query("DELETE FROM CouncilReviewRelay crr " +
            "WHERE crr.councilReviewPost.councilReviewPostId = :postId")
    void deleteByCouncilReviewPostId(@Param("postId") Long postId);

    // 특정 활동 후기에서 특정 사용자의 릴레이 삭제 (참여자 제거 시)
    @Modifying
    @Query("DELETE FROM CouncilReviewRelay crr " +
            "WHERE crr.councilReviewPost.councilReviewPostId = :postId " +
            "AND crr.writerUser.userId = :userId")
    void deleteByCouncilReviewPostIdAndWriterUserId(
            @Param("postId") Long postId,
            @Param("userId") Long userId
    );

    // relay_order 기준으로 정렬된 릴레이 목록 조회 (재정렬용)
    @Query("SELECT crr FROM CouncilReviewRelay crr " +
            "WHERE crr.councilReviewPost.councilReviewPostId = :postId " +
            "ORDER BY crr.relayOrder ASC")
    List<CouncilReviewRelay> findByCouncilReviewPostIdOrderByRelayOrder(@Param("postId") Long postId);

    // 여러 게시글의 릴레이 수를 한 번에 조회 (N+1 방지)
    @Query("SELECT r.councilReviewPost.councilReviewPostId, COUNT(r) " +
            "FROM CouncilReviewRelay r " +
            "WHERE r.councilReviewPost.councilReviewPostId IN :postIds " +
            "GROUP BY r.councilReviewPost.councilReviewPostId")
    List<Object[]> countByPostIdsRaw(@Param("postIds") List<Long> postIds);

    default Map<Long, Long> countByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Object[]> results = countByPostIdsRaw(postIds);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }
}