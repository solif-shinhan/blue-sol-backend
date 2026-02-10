package com.solif.backend.domain.mypage.service;

import com.solif.backend.domain.auth.code.AuthErrorCode;
import com.solif.backend.domain.councilreview.entity.CouncilReviewParticipant;
import com.solif.backend.domain.councilreview.repository.CouncilReviewParticipantRepository;
import com.solif.backend.domain.file.entity.AttachmentPurpose;
import com.solif.backend.domain.file.entity.FileAttachment;
import com.solif.backend.domain.file.entity.FileTargetType;
import com.solif.backend.domain.file.repository.FileAttachmentRepository;
import com.solif.backend.domain.mission.entity.MissionCategory;
import com.solif.backend.domain.mission.entity.MissionStatus;
import com.solif.backend.domain.mission.repository.UserMissionRepository;
import com.solif.backend.domain.mypage.code.MyPageErrorCode;
import com.solif.backend.domain.mypage.dto.MyPageResponse;
import com.solif.backend.domain.profile.entity.UserProfile;
import com.solif.backend.domain.profile.repository.UserProfileRepository;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserMissionRepository userMissionRepository;
    private final CouncilReviewParticipantRepository councilReviewParticipantRepository;
    private final FileAttachmentRepository fileAttachmentRepository;

    @Value("${cloud.aws.region.static}")
    private String region;

    // 마이페이지 조회
    public MyPageResponse getMyPage(Long userId) {
        // 사용자 조회
        User user = findUserById(userId);

        // 프로필 조회
        UserProfile profile = userProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new CustomException(MyPageErrorCode.PROFILE_NOT_FOUND));

        // 활동 대시보드 구성
        MyPageResponse.Dashboard dashboard = buildDashboard(user);

        // 나의 지난 활동 (자치회 활동 후기 최신순 3개)
        List<MyPageResponse.RecentCouncilReview> recentReviews = getRecentCouncilReviews(userId);

        return MyPageResponse.of(
                user.getName(),
                profile.getSolidGoalName(),
                dashboard,
                recentReviews
        );
    }

    // ===== 활동 대시보드 구성 =====
    private MyPageResponse.Dashboard buildDashboard(User user) {
        // 카테고리별 완료된 미션 개수 조회
        Long connectCount = userMissionRepository.countByUserAndCategoryAndStatus(
                user, MissionCategory.CONNECT, MissionStatus.COMPLETED
        );
        Long growCount = userMissionRepository.countByUserAndCategoryAndStatus(
                user, MissionCategory.GROW, MissionStatus.COMPLETED
        );
        Long impactCount = userMissionRepository.countByUserAndCategoryAndStatus(
                user, MissionCategory.IMPACT, MissionStatus.COMPLETED
        );

        // 점수 계산 (미션 1개 완료 = 10점)
        int connectionScore = connectCount.intValue() * 10;
        int growthScore = growCount.intValue() * 10;
        int contributionScore = impactCount.intValue() * 10;
        int totalScore = connectionScore + growthScore + contributionScore;

        // 페르소나 타입 결정
        String personaType = determinePersonaType(connectionScore, growthScore, contributionScore, totalScore);

        return MyPageResponse.Dashboard.builder()
                .connection(connectionScore)
                .growth(growthScore)
                .contribution(contributionScore)
                .total(totalScore)
                .personaType(personaType)
                .build();
    }

    // 페르소나 타입 결정 로직
    private String determinePersonaType(int connection, int growth, int contribution, int total) {
        // 1순위: 전체 90점 만점
        if (total == 90) {
            return "푸른SOL 마스터";
        }

        // 2순위: 2개 영역 동점 (20점 이상)
        int maxScore = Math.max(connection, Math.max(growth, contribution));
        long countMax = 0;
        if (connection == maxScore) countMax++;
        if (growth == maxScore) countMax++;
        if (contribution == maxScore) countMax++;

        if (countMax >= 2 && maxScore >= 20) {
            return "밸런스형 리더";
        }

        // 3순위: 단일 영역 최고점
        if (connection > growth && connection > contribution) {
            return "마당발 네트워커";
        } else if (growth > connection && growth > contribution) {
            return "탐구하는 학구파";
        } else if (contribution > connection && contribution > growth) {
            return "세상을 바꾸는 행동대장";
        }

        // 기본값 (모두 0점이거나 동점인 경우)
        return "푸른SOL 새싹";
    }

    // ===== 나의 지난 활동 (자치회 활동 후기 최신순 3개) =====
    private List<MyPageResponse.RecentCouncilReview> getRecentCouncilReviews(Long userId) {
        List<CouncilReviewParticipant> participants =
                councilReviewParticipantRepository.findByUserIdWithPostOrderByCreatedAtDesc(userId);

        // 빈 결과 처리
        if (participants.isEmpty()) {
            return List.of();
        }

        // 최신순 3개만 가져오기
        List<CouncilReviewParticipant> topThree = participants.stream()
                .limit(3)
                .toList();

        // CouncilReviewPost ID 목록 추출
        List<Long> councilReviewPostIds = topThree.stream()
                .map(p -> p.getCouncilReviewPost().getCouncilReviewPostId())
                .toList();

        // 썸네일 이미지 배치 조회 (sortOrder=1, purpose=POST_ATTACHMENT)
        List<FileAttachment> thumbnails = fileAttachmentRepository
                .findByFileTargetTypeAndFileTargetIdInAndSortOrderAndPurpose(
                        FileTargetType.COUNCIL_POST,  // ← 이 부분 수정!
                        councilReviewPostIds,  // ← postId가 아니라 councilReviewPostId
                        1,
                        AttachmentPurpose.POST_ATTACHMENT
                );

        // councilReviewPostId -> thumbnailUrl 맵 생성
        Map<Long, String> thumbnailMap = thumbnails.stream()
                .collect(Collectors.toMap(
                        FileAttachment::getFileTargetId,
                        fa -> fa.getFile().getUrl(region),
                        (existing, replacement) -> existing  // 중복 키 발생 시 첫 번째 값 유지
                ));

        // Response 구성
        return topThree.stream()
                .map(participant -> {
                    Long councilReviewPostId = participant.getCouncilReviewPost().getCouncilReviewPostId();
                    String thumbnailUrl = thumbnailMap.get(councilReviewPostId);

                    return MyPageResponse.RecentCouncilReview.builder()
                            .councilReviewPostId(councilReviewPostId)
                            .postId(participant.getCouncilReviewPost().getPost().getPostId())
                            .title(participant.getCouncilReviewPost().getPost().getPostTitle())
                            .activityDate(participant.getCouncilReviewPost().getActivityDate())
                            .thumbnailImageUrl(thumbnailUrl)
                            .createdAt(participant.getCouncilReviewPost().getCreatedAt())
                            .build();
                })
                .toList();
    }

    // ===== Helper Methods =====
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
    }
}