package com.solif.backend.domain.councilreview.service;

import com.solif.backend.domain.councilreview.code.CouncilReviewErrorCode;
import com.solif.backend.domain.councilreview.dto.response.CouncilReviewQuestionResponse;
import com.solif.backend.domain.councilreview.entity.CouncilReviewQuestion;
import com.solif.backend.domain.councilreview.repository.CouncilReviewQuestionRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouncilReviewQuestionService {

    private final CouncilReviewQuestionRepository questionRepository;
    private final Random random = new Random();

    /**
     * 랜덤 질문 조회
     * 이미 사용된 질문을 제외하고 활성화된 질문 중 랜덤으로 1개 조회
     */
    public CouncilReviewQuestionResponse getRandomQuestion(List<Long> excludeQuestionIds) {
        log.info("랜덤 질문 조회 - excludeQuestionIds: {}", excludeQuestionIds);

        // 활성화된 질문 목록 조회 (제외 목록 제외)
        List<CouncilReviewQuestion> availableQuestions;

        if (excludeQuestionIds == null || excludeQuestionIds.isEmpty()) {
            availableQuestions = questionRepository.findAllActiveQuestions();
        } else {
            availableQuestions = questionRepository.findActiveQuestionsExcluding(excludeQuestionIds);
        }

        // 사용 가능한 질문이 없으면 예외
        if (availableQuestions.isEmpty()) {
            throw new CustomException(CouncilReviewErrorCode.NO_AVAILABLE_QUESTIONS);
        }

        // 랜덤으로 1개 선택
        int randomIndex = random.nextInt(availableQuestions.size());
        CouncilReviewQuestion selectedQuestion = availableQuestions.get(randomIndex);

        log.info("선택된 질문 ID: {}, 내용: {}", selectedQuestion.getCouncilReviewQuestionId(), selectedQuestion.getQuestionText());

        return CouncilReviewQuestionResponse.from(selectedQuestion);
    }

    /**
     * 질문 검증 (활성화 여부 확인)
     */
    public CouncilReviewQuestion validateAndGetQuestion(Long questionId) {
        log.info("질문 검증 - questionId: {}", questionId);

        // 질문 존재 확인
        CouncilReviewQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(CouncilReviewErrorCode.QUESTION_NOT_FOUND));

        // 활성화 여부 확인
        if (!question.isActive()) {
            throw new CustomException(CouncilReviewErrorCode.QUESTION_NOT_ACTIVE);
        }

        return question;
    }
}