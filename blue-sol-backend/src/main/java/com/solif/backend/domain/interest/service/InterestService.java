package com.solif.backend.domain.interest.service;

import com.solif.backend.domain.interest.dto.InterestRequest;
import com.solif.backend.domain.interest.dto.InterestResponse;
import com.solif.backend.domain.interest.entity.UserInterest;
import com.solif.backend.domain.interest.exception.InterestErrorCode;
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
            "봉사활동", "여행", "축구", "농구", "야구",
            "문화생활", "독서", "스터디", "금융"
    );

    @Transactional
    public InterestResponse registerInterests(Long userId, InterestRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(InterestErrorCode.USER_NOT_FOUND));

        // 2. 카테고리 유효성 검증
        validateCategories(request.getCategoryNames());

        // 3. 기존 관심사 삭제 (덮어쓰기)
        userInterestRepository.deleteAllByUser_UserId(userId);

        // 4. 새 관심사 저장
        List<UserInterest> interests = request.getCategoryNames().stream()
                .map(categoryName -> UserInterest.builder()
                        .user(user)
                        .categoryName(categoryName)
                        .build())
                .toList();

        userInterestRepository.saveAll(interests);

        // 5. 응답 반환
        return InterestResponse.of(interests.size(), request.getCategoryNames());
    }

    private void validateCategories(List<String> categoryNames) {
        for (String category : categoryNames) {
            if (!VALID_CATEGORIES.contains(category)) {
                throw new CustomException(InterestErrorCode.INVALID_CATEGORY);
            }
        }
    }
}
