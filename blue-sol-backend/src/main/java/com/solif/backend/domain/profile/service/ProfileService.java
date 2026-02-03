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
import com.solif.backend.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final QrCodeService qrCodeService;
    private final ObjectMapper objectMapper;
    private final S3Service s3Service;

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
        QrCodeService.QrResult qrResult = qrCodeService.generateAndUpload(userId);

        // 4. 목표 리스트를 JSON 문자열로 변환
        String mainGoalJson = convertListToJson(request.getMainGoals());

        // 5. 프로필 생성
        UserProfile profile = UserProfile.builder()
                .user(user)
                .userCharacter(request.getUserCharacter())
                .backgroundPattern(request.getBackgroundPattern())
                .mainGoal(mainGoalJson)
                .solidGoalName(request.getSolidGoalName())
                .qrCodeData(qrResult.qrData())
                .qrImageUrl(qrResult.imageUrl())
                .build();

        userProfileRepository.save(profile);

        return ProfileCreateResponse.of(profile.getProfileId(), qrResult.imageUrl());
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
        List<String> mainGoals = profile.getMainGoal() != null
                ? convertJsonToList(profile.getMainGoal())
                : List.of();

        return ProfileResponse.builder()
                .profileId(profile.getProfileId())
                .userName(profile.getUser().getName())
                .mainGoals(mainGoals)
                .solidGoalName(profile.getSolidGoalName())
                .userCharacter(profile.getUserCharacter())
                .characterImageUrl(String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, profile.getUserCharacter()))
                .backgroundPattern(profile.getBackgroundPattern())
                .backgroundImageUrl(String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, profile.getBackgroundPattern()))
                .qrCodeUrl(profile.getQrImageUrl())   // S3 이미지 URL 반환
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

    //배경화면 리스트 조회
    public List<BackgroundListResponse> getBackgroundList() {
        List<String> fileKeys = s3Service.getFileList(bucket, "selectbgs/");
        String baseUrl = String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);

        return fileKeys.stream()
                .filter(this::isImageFile)
                .map(key -> BackgroundListResponse.of(key, baseUrl + key))
                .toList();
    }

    //캐릭터 리스트 조회
    public List<CharacterListResponse> getCharacterList() {
        List<String> fileKeys = s3Service.getFileList(bucket, "characters/");
        String baseUrl = String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);

        return fileKeys.stream()
                .filter(this::isImageFile)
                .map(key -> CharacterListResponse.of(key, baseUrl + key))
                .toList();
    }

    /**
     * 확장자를 통해 이미지 파일 여부를 확인합니다.
     */
    private boolean isImageFile(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".png") ||
                lowerCaseName.endsWith(".jpg") ||
                lowerCaseName.endsWith(".jpeg") ||
                lowerCaseName.endsWith(".svg") ||
                lowerCaseName.endsWith(".webp");
    }
}
