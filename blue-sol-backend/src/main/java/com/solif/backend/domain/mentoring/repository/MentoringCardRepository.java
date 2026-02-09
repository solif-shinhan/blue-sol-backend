package com.solif.backend.domain.mentoring.repository;

import com.solif.backend.domain.mentoring.entity.MentoringCard;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MentoringCardRepository extends JpaRepository<MentoringCard, Long> {

    // 내가 보낸 멘토링 엽서 목록 조회 (최신순)
    @Query("SELECT mc FROM MentoringCard mc " +
            "JOIN FETCH mc.sender " +
            "WHERE mc.sender.userId = :userId " +
            "ORDER BY mc.createdAt DESC")
    Slice<MentoringCard> findSentCards(@Param("userId") Long userId, Pageable pageable);

    // 관리자용: 모든 멘토링 엽서 목록 조회 (향후 구현)
    @Query("SELECT mc FROM MentoringCard mc " +
            "JOIN FETCH mc.sender " +
            "ORDER BY mc.createdAt DESC")
    Slice<MentoringCard> findAllCards(Pageable pageable);
}