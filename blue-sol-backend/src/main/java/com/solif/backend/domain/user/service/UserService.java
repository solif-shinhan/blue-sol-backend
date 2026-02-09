package com.solif.backend.domain.user.service;

import com.solif.backend.domain.auth.code.AuthErrorCode;
import com.solif.backend.domain.council.entity.CouncilMember;
import com.solif.backend.domain.council.repository.CouncilMemberRepository;
import com.solif.backend.domain.profile.entity.UserProfile;
import com.solif.backend.domain.profile.repository.UserProfileRepository;
import com.solif.backend.domain.user.dto.UserMeResponse;
import com.solif.backend.domain.user.dto.UserSearchResponse;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final UserProfileRepository userProfileRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    // 내 정보 조회
    public UserMeResponse getMyInfo(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        return UserMeResponse.from(user);
    }

    // 사용자 검색
    public List<UserSearchResponse> searchUsers(String keyword) {
        log.info("사용자 검색 - keyword: {}", keyword);

        // 키워드가 비어있으면 빈 리스트 반환
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        // 최대 20명까지만 조회
        Pageable pageable = PageRequest.of(0, 20);
        List<User> users = userRepository.searchByName(keyword.trim(), pageable);

        if (users.isEmpty()) {
            return List.of();
        }

        // 사용자 ID 목록
        List<Long> userIds = users.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());

        // 한 번의 쿼리로 모든 멤버십 조회 (N+1 방지)
        List<CouncilMember> memberships = councilMemberRepository.findByUserIdIn(new HashSet<>(userIds));

        // userId -> CouncilMember 매핑
        Map<Long, CouncilMember> membershipMap = memberships.stream()
                .collect(Collectors.toMap(
                        cm -> cm.getUser().getUserId(),
                        Function.identity()
                ));

        // 한 번의 쿼리로 모든 프로필 조회 (N+1 방지)
        List<UserProfile> profiles = userProfileRepository.findByUser_UserIdIn(userIds);

        // userId -> UserProfile 매핑
        Map<Long, UserProfile> profileMap = profiles.stream()
                .collect(Collectors.toMap(
                        profile -> profile.getUser().getUserId(),
                        Function.identity()
                ));

        // DTO 변환
        return users.stream()
                .map(user -> {
                    UserProfile profile = profileMap.get(user.getUserId());

                    String userCharacter = profile != null ? profile.getUserCharacter() : null;
                    String characterImageUrl = buildS3Url(userCharacter);

                    String backgroundPattern = profile != null ? profile.getBackgroundPattern() : null;
                    String backgroundImageUrl = buildS3Url(backgroundPattern);

                    return UserSearchResponse.from(
                            user,
                            membershipMap.get(user.getUserId()),
                            userCharacter,
                            characterImageUrl,
                            backgroundPattern,
                            backgroundImageUrl
                    );
                })
                .collect(Collectors.toList());
    }

    // ===== Helper Methods =====

    // S3 URL 빌더
    private String buildS3Url(String key) {
        if (key == null || key.isBlank()) return null;
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }
}
