package com.solif.backend.domain.mentoring.repository;

import com.solif.backend.domain.mentoring.entity.MentoringRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MentoringRequestRepository extends JpaRepository<MentoringRequest, Long> {

    // 내가 보낸 신청서 목록 조회 (전체 - 답변 유무 상관없이)
    @Query("SELECT mr FROM MentoringRequest mr " +
            "JOIN FETCH mr.mentor " +
            "WHERE mr.menteeUser.userId = :userId " +
            "ORDER BY mr.createdAt DESC")
    Slice<MentoringRequest> findSentRequests(@Param("userId") Long userId, Pageable pageable);

    // 받은 신청서 목록 조회 (답변이 있는 것만)
    @Query("SELECT mr FROM MentoringRequest mr " +
            "JOIN FETCH mr.mentor " +
            "WHERE mr.menteeUser.userId = :userId " +
            "AND mr.adminReply IS NOT NULL " +
            "ORDER BY mr.repliedAt DESC")
    Slice<MentoringRequest> findReceivedRequests(@Param("userId") Long userId, Pageable pageable);

    // 신청서 상세 조회 (멘토 정보 포함)
    @Query("SELECT mr FROM MentoringRequest mr " +
            "JOIN FETCH mr.mentor m " +
            "JOIN FETCH mr.menteeUser " +
            "WHERE mr.mentoringRequestId = :requestId")
    Optional<MentoringRequest> findByIdWithDetails(@Param("requestId") Long requestId);
}