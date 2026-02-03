package com.solif.backend.global.config;

import com.solif.backend.domain.councilreview.entity.CouncilReviewQuestion;
import com.solif.backend.domain.councilreview.repository.CouncilReviewQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouncilReviewQuestionDataInitializer implements CommandLineRunner {

    private final CouncilReviewQuestionRepository questionRepository;

    @Override
    public void run(String... args) throws Exception {
        // 이미 질문 데이터가 있으면 스킵
        if (questionRepository.count() > 0) {
            log.info("자치회 후기 질문 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("자치회 후기 질문 초기 데이터 삽입 시작");

        List<String> questions = List.of(
                "오늘 여러분의 마음을 채워준 한 끼는 무엇이었나요?",
                "오늘의 날씨와 모임의 분위기는 잘 어울렸나요?",
                "함께한 시간 중 가장 웃음이 터졌던 순간은 언제였나요?",
                "오늘 찍은 사진에 담긴 이야기를 들려주세요.",
                "오늘 나눈 대화 중 가장 공감되었던 이야기는 무엇이었나요?",
                "서로의 고민을 나누며 발견한 우리의 공통점은 무엇이었나요?",
                "오늘 만난 서로에게서 발견한 멋진 점은 무엇이었나요?",
                "오늘의 만남이 내 꿈에 어떤 작은 영감을 주었나요?",
                "오늘 대화를 통해 새롭게 다짐한 목표가 생겼나요?",
                "다음 만남을 기약하며 꼭 해보고 싶은 활동이 있다면 무엇인가요?",
                "오늘 함께한 동료들에게 보내는 짧은 응원의 한마디를 남겨주세요.",
                "1년 뒤 우리는 서로에게 어떤 모습으로 기억될까요?",
                "오늘의 에너지를 받아 내일 바로 실천하고 싶은 작은 행동은 무엇인가요?"
        );

        questions.forEach(questionText -> {
            CouncilReviewQuestion question = CouncilReviewQuestion.builder()
                    .questionText(questionText)
                    .isActive(true)
                    .build();
            questionRepository.save(question);
        });

        log.info("자치회 후기 질문 초기 데이터 삽입 완료: {} 개", questions.size());
    }
}