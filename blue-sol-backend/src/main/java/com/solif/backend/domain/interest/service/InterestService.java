package com.solif.backend.domain.interest.service;

import com.solif.backend.domain.interest.dto.InterestRequest;
import com.solif.backend.domain.interest.dto.InterestResponse;
import com.solif.backend.domain.interest.entity.UserInterest;
import com.solif.backend.domain.interest.code.InterestErrorCode;
import com.solif.backend.domain.interest.repository.UserInterestRepository;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterestService {

    private final UserInterestRepository userInterestRepository;
    private final UserRepository userRepository;

    // 유효한 카테고리 목록
    private static final Set<String> VALID_CATEGORIES = Set.of(
            "맛집", "언어공부", "영화", "게임", "사진",
            "봉사활동", "여행", "축구", "농구", "야구", "자격증", "예술", "산책", "문화생활", "독서", "스터디", "경제"
    );

    @Transactional
    public InterestResponse registerInterests(Long userId, InterestRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(InterestErrorCode.USER_NOT_FOUND));

        // 2. 중복 제거 후 카테고리 목록 생성
        List<String> uniqueCategories = request.getCategoryNames().stream()
                .distinct()
                .toList();

        // 3. 카테고리 유효성 검증
        validateCategories(request.getCategoryNames());

        // 4. 기존 관심사 삭제 (덮어쓰기)
        userInterestRepository.deleteAllByUser_UserId(userId);

        // 5. 새 관심사 저장
        List<UserInterest> interests = request.getCategoryNames().stream()
                .map(categoryName -> UserInterest.builder()
                        .user(user)
                        .categoryName(categoryName)
                        .build())
                .toList();

        userInterestRepository.saveAll(interests);

        // 6. 응답 반환
        return InterestResponse.of(interests.size(), uniqueCategories);
    }

    private void validateCategories(List<String> categoryNames) {
        for (String category : categoryNames) {
            if (!VALID_CATEGORIES.contains(category)) {
                throw new CustomException(InterestErrorCode.INVALID_CATEGORY);
            }
        }
    }
}
