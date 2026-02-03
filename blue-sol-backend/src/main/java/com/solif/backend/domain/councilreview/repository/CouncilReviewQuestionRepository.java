package com.solif.backend.domain.councilreview.repository;

import com.solif.backend.domain.councilreview.entity.CouncilReviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouncilReviewQuestionRepository extends JpaRepository<CouncilReviewQuestion, Long> {

    // 활성화된 질문 목록 조회
    @Query("SELECT q FROM CouncilReviewQuestion q " +
            "WHERE q.isActive = true " +
            "ORDER BY q.createdAt ASC")
    List<CouncilReviewQuestion> findAllActiveQuestions();

    // 활성화된 질문 중 제외 목록을 제외하고 조회
    @Query("SELECT q FROM CouncilReviewQuestion q " +
            "WHERE q.isActive = true " +
            "AND q.councilReviewQuestionId NOT IN :excludeIds " +
            "ORDER BY q.createdAt ASC")
    List<CouncilReviewQuestion> findActiveQuestionsExcluding(
            @Param("excludeIds") List<Long> excludeIds
    );

    // 특정 질문이 활성화 상태인지 확인
    @Query("SELECT COUNT(q) > 0 FROM CouncilReviewQuestion q " +
            "WHERE q.councilReviewQuestionId = :questionId " +
            "AND q.isActive = true")
    boolean isActiveQuestion(@Param("questionId") Long questionId);

    // ID로 활성화된 질문 조회
    @Query("SELECT q FROM CouncilReviewQuestion q " +
            "WHERE q.councilReviewQuestionId = :questionId " +
            "AND q.isActive = true")
    Optional<CouncilReviewQuestion> findActiveQuestionById(@Param("questionId") Long questionId);
}