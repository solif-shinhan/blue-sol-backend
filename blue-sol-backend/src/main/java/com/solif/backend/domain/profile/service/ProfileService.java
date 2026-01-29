package com.solif.backend.domain.profile.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solif.backend.domain.interest.entity.UserInterest;
import com.solif.backend.domain.interest.repository.UserInterestRepository;
import com.solif.backend.domain.profile.dto.*;
import com.solif.backend.domain.profile.entity.UserProfile;
import com.solif.backend.domain.profile.exception.ProfileErrorCode;
import com.solif.backend.domain.profile.repository.UserProfileRepository;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import com.solif.backend.global.qr.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final QrCodeService qrCodeService;
    private final ObjectMapper objectMapper;

    // 프로필 생성
    @Transactional
    public ProfileCreateResponse createProfile(Long userId, ProfileCreateRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ProfileErrorCode.USER_NOT_FOUND));

        // 2. 프로필 중복 체크
        if (userProfileRepository.existsByUser_UserId(userId)) {
            throw new CustomException(ProfileErrorCode.PROFILE_ALREADY_EXISTS);
        }

        // 3. QR 코드 생성 및 S3 업로드
        String qrCodeUrl = qrCodeService.generateAndUpload(userId);

        // 4. 목표 리스트를 JSON 문자열로 변환
        String mainGoalJson = convertListToJson(request.getMainGoals());

        // 5. 프로필 생성
        UserProfile profile = UserProfile.builder()
                .user(user)
                .userCharacter(request.getUserCharacter())
                .backgroundPattern(request.getBackgroundPattern())
                .mainGoal(mainGoalJson)
                .solidGoalName(request.getSolidGoalName())
                .qrCodeData(qrCodeUrl)
                .build();

        userProfileRepository.save(profile);

        return ProfileCreateResponse.of(profile.getProfileId(), qrCodeUrl);
    }

    // 프로필 수정
    @Transactional
    public ProfileUpdateResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        // 1. 프로필 조회
        UserProfile profile = userProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new CustomException(ProfileErrorCode.PROFILE_NOT_FOUND));

        // 2. 목표 JSON 변환
        String mainGoalJson = request.getMainGoals() != null
                ? convertListToJson(request.getMainGoals())
                : null;

        // 3. 프로필 업데이트
        profile.update(mainGoalJson, request.getUserCharacter(), request.getSolidGoalName(), request.getBackgroundPattern());

        // 4. 관심사 업데이트 (요청에 관심사가 있는 경우만)
        if (request.getInterests() != null) {
            User user = profile.getUser();
            // 기존 관심사 삭제
            userInterestRepository.deleteAllByUser_UserId(userId);
            // 새 관심사 저장
            List<UserInterest> newInterests = request.getInterests().stream()
                    .map(categoryName -> UserInterest.builder()
                            .user(user)
                            .categoryName(categoryName)
                            .build())
                    .toList();
            userInterestRepository.saveAll(newInterests);
        }

        return ProfileUpdateResponse.of(profile.getProfileId());
    }

    // 프로필 조회
    public ProfileResponse getProfile(Long userId) {
        // 1. 프로필 조회
        UserProfile profile = userProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new CustomException(ProfileErrorCode.PROFILE_NOT_FOUND));

        // 2. 관심사 조회
        List<String> interests = userInterestRepository.findAllByUser_UserId(userId)
                .stream()
                .map(UserInterest::getCategoryName)
                .toList();

        // 3. 목표 JSON을 리스트로 변환
        List<String> mainGoals = convertJsonToList(profile.getMainGoal());

        return ProfileResponse.builder()
                .profileId(profile.getProfileId())
                .userName(profile.getUser().getName())
                .mainGoals(mainGoals)
                .solidGoalName(profile.getSolidGoalName())
                .userCharacter(profile.getUserCharacter())
                .backgroundPattern(profile.getBackgroundPattern())
                .qrCodeUrl(profile.getQrCodeData())
                .interests(interests)
                .build();
    }

    private String convertListToJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }

    private List<String> convertJsonToList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }
}
