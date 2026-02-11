package com.solif.backend.domain.mission.service;

import com.solif.backend.domain.auth.code.AuthErrorCode;
import com.solif.backend.domain.comment.entity.Comment;
import com.solif.backend.domain.comment.repository.CommentRepository;
import com.solif.backend.domain.file.entity.AttachmentPurpose;
import com.solif.backend.domain.file.entity.FileAttachment;
import com.solif.backend.domain.file.entity.FileTargetType;
import com.solif.backend.domain.file.repository.FileAttachmentRepository;
import com.solif.backend.domain.message.entity.Message;
import com.solif.backend.domain.message.repository.MessageRepository;
import com.solif.backend.domain.mission.code.MissionErrorCode;
import com.solif.backend.domain.mission.dto.MissionProgressResponse;
import com.solif.backend.domain.mission.dto.PineconeEarnResponse;
import com.solif.backend.domain.mission.dto.PineconeMemoryResponse;
import com.solif.backend.domain.mission.entity.*;
import com.solif.backend.domain.mission.repository.MissionRepository;
import com.solif.backend.domain.mission.repository.PineconeMemoryRepository;
import com.solif.backend.domain.mission.repository.UserMissionRepository;
import com.solif.backend.domain.mission.repository.UserPineconeRepository;
import com.solif.backend.domain.notification.entity.Notification;
import com.solif.backend.domain.notification.entity.NotificationType;
import com.solif.backend.domain.notification.repository.NotificationRepository;
import com.solif.backend.domain.post.entity.Post;
import com.solif.backend.domain.post.entity.PostCategory;
import com.solif.backend.domain.post.repository.PostRepository;
import com.solif.backend.domain.profile.entity.UserProfile;
import com.solif.backend.domain.profile.repository.UserProfileRepository;
import com.solif.backend.domain.scholarshipprogrampost.entity.ScholarshipProgramPost;
import com.solif.backend.domain.scholarshipprogrampost.repository.ScholarshipProgramPostRepository;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionService {

    private final MissionRepository missionRepository;
    private final UserMissionRepository userMissionRepository;
    private final UserPineconeRepository userPineconeRepository;
    private final PineconeMemoryRepository pineconeMemoryRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final NotificationRepository notificationRepository;
    private final MessageRepository messageRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ScholarshipProgramPostRepository scholarshipProgramPostRepository;
    private final FileAttachmentRepository fileAttachmentRepository;

    @Value("${cloud.aws.region.static}")
    private String region;

    // ===== 미션 진행 상황 조회 =====
    public MissionProgressResponse getMissionProgress(Long userId) {
        User user = findUserById(userId);
        String currentSeason = getCurrentSeasonKey();

        // 획득한 솔방울 개수
        Long earnedPineconeCount = userPineconeRepository.countByUserAndSeasonKey(user, currentSeason);

        // D-Day 계산 (상반기 종료까지)
        Integer daysUntilEnd = calculateDaysUntilSeasonEnd();

        // 사용자 미션 목록 조회
        List<UserMission> userMissions = userMissionRepository.findByUserWithMission(user);

        // 카테고리별 진행 상황
        List<MissionProgressResponse.CategoryProgress> categoryProgress = buildCategoryProgress(user, currentSeason, userMissions);

        // 이번주 미션 리스트 (각 카테고리의 현재 진행 중인 미션)
        List<MissionProgressResponse.WeeklyMissionCard> weeklyMissions = buildWeeklyMissions(userMissions);

        // 장학 프로그램 최신 3개
        List<MissionProgressResponse.ScholarshipProgramCard> scholarshipPrograms = buildScholarshipPrograms();

        return MissionProgressResponse.builder()
                .currentSeason(currentSeason)
                .earnedPineconeCount(earnedPineconeCount.intValue())
                .daysUntilSeasonEnd(daysUntilEnd)
                .categoryProgress(categoryProgress)
                .weeklyMissions(weeklyMissions)
                .scholarshipPrograms(scholarshipPrograms)
                .build();
    }

    // ===== 솔방울 획득 (받기 버튼) =====
    @Transactional
    public PineconeEarnResponse earnPinecone(Long userId, MissionCategory category) {
        User user = findUserById(userId);
        String currentSeason = getCurrentSeasonKey();

        // 이미 획득했는지 확인
        if (userPineconeRepository.existsByUserAndPineconeCategoryAndSeasonKey(user, category, currentSeason)) {
            throw new CustomException(MissionErrorCode.PINECONE_ALREADY_EARNED);
        }

        // 해당 카테고리의 미션 3개가 모두 완료되었는지 확인
        Long completedCount = userMissionRepository.countByUserAndCategoryAndStatus(
                user, category, MissionStatus.COMPLETED
        );

        if (completedCount < 3) {
            throw new CustomException(MissionErrorCode.CATEGORY_MISSIONS_NOT_COMPLETED);
        }

        // 솔방울 생성
        UserPinecone pinecone = UserPinecone.builder()
                .user(user)
                .pineconeCategory(category)
                .seasonKey(currentSeason)
                .build();
        userPineconeRepository.save(pinecone);

        // 솔방울 추억 생성 (완료된 미션 3개의 추억 저장)
        List<UserMission> completedMissions = userMissionRepository
                .findByUserAndMission_MissionCategory(user, category)
                .stream()
                .filter(UserMission::isCompleted)
                .limit(3)
                .toList();

        for (UserMission userMission : completedMissions) {
            savePineconeMemory(pinecone, userMission);
        }

        // TODO: 알림 발송
        // notificationService.send(userId, ...);

        log.info("솔방울 획득 - userId: {}, category: {}, season: {}", userId, category, currentSeason);

        return PineconeEarnResponse.builder()
                .pineconeId(pinecone.getUserPineconeId())
                .category(category)
                .categoryName(getCategoryName(category))
                .seasonKey(currentSeason)
                .earnedAt(pinecone.getEarnedAt())
                .build();
    }

    // ===== 솔방울 추억 회상 =====
    public PineconeMemoryResponse getPineconeMemory(Long userId, MissionCategory category) {
        User user = findUserById(userId);
        String currentSeason = getCurrentSeasonKey();

        // 솔방울 조회
        UserPinecone pinecone = userPineconeRepository.findByUserAndPineconeCategoryAndSeasonKey(
                user, category, currentSeason
        ).orElseThrow(() -> new CustomException(MissionErrorCode.PINECONE_NOT_FOUND));

        // 완료된 미션 3개 조회 (UserMission 기준 - sequenceOrder 순)
        List<UserMission> completedUserMissions = userMissionRepository
                .findByUserAndMission_MissionCategory(user, category)
                .stream()
                .filter(UserMission::isCompleted)
                .sorted((um1, um2) -> Integer.compare(
                        um1.getMission().getSequenceOrder(),
                        um2.getMission().getSequenceOrder()
                ))
                .limit(3)
                .toList();

        // 추억 조회
        List<PineconeMemory> memories = pineconeMemoryRepository.findByUserPineconeOrderByCreatedAtAsc(pinecone);

        // conditionType 기준으로 Map 생성 (빠른 조회용)
        Map<MissionConditionType, PineconeMemory> memoryMap = memories.stream()
                .collect(Collectors.toMap(PineconeMemory::getConditionType, m -> m, (m1, m2) -> m1));

        // 완료한 미션 3개 구성
        List<PineconeMemoryResponse.CompletedMission> completedMissions = new ArrayList<>();

        for (UserMission userMission : completedUserMissions) {
            Mission mission = userMission.getMission();
            MissionConditionType conditionType = mission.getConditionType();
            String missionTitle = mission.getMissionTitle();

            // 해당 미션의 PineconeMemory가 있는지 확인
            PineconeMemory memory = memoryMap.get(conditionType);

            boolean hasMemoryDetail = false;
            PineconeMemoryResponse.MemoryDetail memoryDetail = null;

            if (memory != null) {
                hasMemoryDetail = hasMemoryDetail(memory.getSourceType());

                if (hasMemoryDetail) {
                    memoryDetail = buildMemoryDetail(memory);
                    // null인 경우 hasMemoryDetail을 false로 변경
                    if (memoryDetail == null) {
                        hasMemoryDetail = false;
                    }
                }
            }


            completedMissions.add(
                    PineconeMemoryResponse.CompletedMission.builder()
                            .missionTitle(missionTitle)
                            .isCompleted(true)
                            .hasMemoryDetail(hasMemoryDetail)
                            .memoryDetail(memoryDetail)
                            .build()
            );
        }

        return PineconeMemoryResponse.builder()
                .category(category)
                .categoryName(getCategoryName(category))
                .completedMissions(completedMissions)
                .build();
    }

    // ===== 미션 체크 & 완료 처리 (각 Service에서 호출) =====
    @Transactional
    public void checkAndCompleteMission(Long userId, MissionConditionType conditionType, Long sourceId) {
        User user = findUserById(userId);

        // 해당 조건 타입의 미션 찾기
        Mission mission = missionRepository.findByConditionType(conditionType)
                .orElse(null);

        if (mission == null) {
            log.warn("미션을 찾을 수 없음 - conditionType: {}", conditionType);
            return;
        }

        // 사용자 미션 조회
        UserMission userMission = userMissionRepository.findByUserAndMission(user, mission)
                .orElse(null);

        if (userMission == null) {
            log.warn("사용자 미션을 찾을 수 없음 - userId: {}, missionId: {}", userId, mission.getMissionId());
            return;
        }

        // 이미 완료된 미션이면 무시
        if (userMission.isCompleted()) {
            return;
        }

        // 미션별 완료 조건 체크
        boolean shouldComplete = checkMissionCondition(userMission, mission, user, sourceId);

        if (shouldComplete) {
            userMission.complete();
            log.info("미션 완료 - userId: {}, missionId: {}, conditionType: {}", userId, mission.getMissionId(), conditionType);
        }
    }

    // ===== 미션 진행도 증가 (카운트 기반 미션용) =====
    @Transactional
    public void incrementMissionProgress(Long userId, MissionConditionType conditionType) {
        User user = findUserById(userId);

        Mission mission = missionRepository.findAll().stream()
                .filter(m -> m.getConditionType() == conditionType)
                .findFirst()
                .orElse(null);

        if (mission == null) {
            return;
        }

        UserMission userMission = userMissionRepository.findByUserAndMission(user, mission)
                .orElse(null);

        if (userMission == null || userMission.isCompleted()) {
            return;
        }

        userMission.incrementProgress();

        // 목표 달성 확인
        int targetCount = getTargetCountFromConditionValue(mission.getConditionValue());
        if (userMission.getProgressCount() >= targetCount) {
            userMission.complete();
            log.info("미션 완료 (카운트 달성) - userId: {}, missionId: {}, count: {}/{}",
                    userId, mission.getMissionId(), userMission.getProgressCount(), targetCount);
        }
    }

    // ===== Helper Methods =====

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
    }

    // 현재 시즌 키 계산 (2026-H1, 2026-H2)
    private String getCurrentSeasonKey() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        String half = (month <= 6) ? "H1" : "H2";
        return year + "-" + half;
    }

    // 시즌 종료까지 남은 일수 계산
    private Integer calculateDaysUntilSeasonEnd() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        LocalDate seasonEnd;
        if (month <= 6) {
            // 상반기: 6월 30일
            seasonEnd = LocalDate.of(year, 6, 30);
        } else {
            // 하반기: 12월 31일
            seasonEnd = LocalDate.of(year, 12, 31);
        }

        return (int) ChronoUnit.DAYS.between(now, seasonEnd);
    }

    // 카테고리별 진행 상황 구성
    private List<MissionProgressResponse.CategoryProgress> buildCategoryProgress(
            User user, String currentSeason, List<UserMission> userMissions
    ) {
        List<MissionProgressResponse.CategoryProgress> result = new ArrayList<>();

        for (MissionCategory category : MissionCategory.values()) {
            Long completedCount = userMissions.stream()
                    .filter(um -> um.getMission().getMissionCategory() == category)
                    .filter(UserMission::isCompleted)
                    .count();

            boolean isPineconeEarned = userPineconeRepository.existsByUserAndPineconeCategoryAndSeasonKey(
                    user, category, currentSeason
            );

            boolean canClaimPinecone = (completedCount >= 3) && !isPineconeEarned;

            result.add(
                    MissionProgressResponse.CategoryProgress.builder()
                            .category(category)
                            .categoryName(getCategoryName(category))
                            .completedCount(completedCount.intValue())
                            .totalCount(3)
                            .isPineconeEarned(isPineconeEarned)
                            .canClaimPinecone(canClaimPinecone)
                            .build()
            );
        }

        return result;
    }

    // 이번주 미션 리스트 구성 (각 카테고리에서 진행 중인 첫 미션)
    private List<MissionProgressResponse.WeeklyMissionCard> buildWeeklyMissions(List<UserMission> userMissions) {
        List<MissionProgressResponse.WeeklyMissionCard> weeklyMissions = new ArrayList<>();

        // 카테고리별로 그룹화
        Map<MissionCategory, List<UserMission>> groupedByCategory = userMissions.stream()
                .collect(Collectors.groupingBy(um -> um.getMission().getMissionCategory()));

        for (MissionCategory category : MissionCategory.values()) {
            List<UserMission> categoryMissions = groupedByCategory.getOrDefault(category, List.of());

            // 진행 중인 첫 번째 미션 찾기 (sequence_order 순)
            UserMission currentMission = categoryMissions.stream()
                    .filter(um -> !um.isCompleted())
                    .min((um1, um2) -> Integer.compare(
                            um1.getMission().getSequenceOrder(),
                            um2.getMission().getSequenceOrder()
                    ))
                    .orElse(null);

            if (currentMission != null) {
                Mission mission = currentMission.getMission();
                int targetCount = getTargetCountFromConditionValue(mission.getConditionValue());

                weeklyMissions.add(
                        MissionProgressResponse.WeeklyMissionCard.builder()
                                .missionId(mission.getMissionId())
                                .category(category)
                                .categoryName(getCategoryName(category))
                                .missionTitle(mission.getMissionTitle())
                                .description(mission.getDescription())
                                .progress(currentMission.getProgressCount() + "/" + targetCount)
                                .currentCount(currentMission.getProgressCount())
                                .targetCount(targetCount)
                                .status(currentMission.getStatus())
                                .isCompleted(currentMission.isCompleted())
                                .build()
                );
            }
        }

        return weeklyMissions;
    }

    // 카테고리명 반환
    private String getCategoryName(MissionCategory category) {
        return switch (category) {
            case CONNECT -> "연결";
            case GROW -> "성장";
            case IMPACT -> "기여";
        };
    }

    // conditionValue에서 목표 카운트 추출 (예: "5" -> 5)
    private int getTargetCountFromConditionValue(String conditionValue) {
        if (conditionValue == null || conditionValue.isEmpty()) {
            return 1;
        }
        try {
            return Integer.parseInt(conditionValue);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    // 미션 완료 조건 체크
    private boolean checkMissionCondition(UserMission userMission, Mission mission, User user, Long sourceId) {
        MissionConditionType conditionType = mission.getConditionType();
        int targetCount = getTargetCountFromConditionValue(mission.getConditionValue());

        return switch (conditionType) {
            case PROFILE_VIEW -> {
                // 프로필 조회 N회
                userMission.incrementProgress();
                yield userMission.getProgressCount() >= targetCount;
            }
            case CONNECTION_ACTION -> {
                // 응원/경험나누기 N회
                userMission.incrementProgress();
                yield userMission.getProgressCount() >= targetCount;
            }
            case MESSAGE -> {
                // 첫 쪽지 발송 (1회만)
                yield userMission.getProgressCount() == 0;
            }
            case PROFILE_COMPLETE -> {
                // 프로필 100% 완성
                UserProfile profile = userProfileRepository.findByUser_UserId(user.getUserId()).orElse(null);
                yield profile != null && isProfileComplete(profile);
            }
            case MESSAGE_THREAD -> {
                // 경험 나누기 → 쪽지 소통
                yield checkMessageThreadCondition(user);
            }
            case POST_VIEW -> {
                // 재단 소식 확인 (N회)
                userMission.incrementProgress();
                yield userMission.getProgressCount() >= targetCount;
            }
            case POST_CREATE, COMMENT_CREATE, MENTORING_COMPLETE -> {
                // 단순 1회 완료
                yield true;
            }
            default -> false;
        };
    }

    // 프로필 완성 여부 체크 (필수/선택 정보 모두 입력)
    private boolean isProfileComplete(UserProfile profile) {
        // 필수: userCharacter, backgroundPattern, mainGoal, solidGoalName
        // 선택: qrImageUrl 등
        // 간단히 Null 체크로 구현 (실제로는 더 정교한 로직 필요)
        return profile.getUserCharacter() != null &&
                profile.getBackgroundPattern() != null &&
                profile.getMainGoal() != null &&
                profile.getSolidGoalName() != null;
    }

    // 경험 나누기 → 쪽지 소통 조건 체크
    private boolean checkMessageThreadCondition(User user) {
        // HELP 알림 조회: 나에게 온 HELP 알림 (내가 receiver인 경우)
        List<Notification> helpNotifications = notificationRepository
                .findByReceiverAndNotificationType(user, NotificationType.HELP);

        if (helpNotifications.isEmpty()) {
            return false;
        }

        // 나에게 HELP를 보낸 사람들(sender)에게 쪽지를 보냈는지 확인
        for (Notification notification : helpNotifications) {
            Long senderId = notification.getTargetId();  // HELP를 보낸 사람의 userId

            // 내가 그 사람에게 쪽지를 보냈는지 확인
            boolean sentMessage = messageRepository.existsBySender_UserIdAndReceiver_UserId(user.getUserId(), senderId);

            if (sentMessage) {
                return true;
            }
        }

        return false;
    }

    // 솔방울 추억 저장
    private void savePineconeMemory(UserPinecone pinecone, UserMission userMission) {
        MissionConditionType conditionType = userMission.getMission().getConditionType();
        PineconeSourceType sourceType = mapConditionTypeToSourceType(conditionType);

        if (sourceType == null) {
            return; // 추억이 없는 미션
        }

        // sourceId 결정 (실제 데이터 조회)
        Long sourceId = findSourceIdForMemory(userMission.getUser(), conditionType);

        if (sourceId != null) {
            PineconeMemory memory = PineconeMemory.builder()
                    .userPinecone(pinecone)
                    .sourceType(sourceType)
                    .sourceId(sourceId)
                    .conditionType(conditionType)
                    .build();
            pineconeMemoryRepository.save(memory);
        }
    }

    // ConditionType -> SourceType 매핑
    private PineconeSourceType mapConditionTypeToSourceType(MissionConditionType conditionType) {
        return switch (conditionType) {
            case MESSAGE, MESSAGE_THREAD -> PineconeSourceType.MESSAGE;
            case POST_CREATE -> PineconeSourceType.POST;
            case COMMENT_CREATE -> PineconeSourceType.COMMENT;
            case MENTORING_COMPLETE -> PineconeSourceType.MENTORING;
            default -> null;
        };
    }

    // 추억 sourceId 찾기
    private Long findSourceIdForMemory(User user, MissionConditionType conditionType) {
        return switch (conditionType) {
            case MESSAGE -> {
                // 첫 번째 보낸 쪽지
                Message message = messageRepository.findFirstBySender_UserIdOrderByCreatedAtAsc(user.getUserId())
                        .orElse(null);
                yield message != null ? message.getMessageId() : null;
            }
            case MESSAGE_THREAD -> {
                // 경험나누기로 연결된 첫 쪽지
                // targetId에 내(sender) userId가 들어있음
                List<Notification> helpNotifications = notificationRepository
                        .findByNotificationTypeAndTargetId(NotificationType.HELP, user.getUserId());

                for (Notification notification : helpNotifications) {
                    Long receiverId = notification.getReceiver().getUserId();

                    Message message = messageRepository.findFirstBySender_UserIdAndReceiver_UserIdOrderByCreatedAtAsc(
                            user.getUserId(), receiverId
                    ).orElse(null);

                    if (message != null) {
                        yield message.getMessageId();
                    }
                }
                yield null;
            }
            case POST_CREATE -> {
                // 첫 번째 작성한 게시글
                Post post = postRepository
                        .findTop1ByAuthor_UserIdAndDeletedAtIsNullOrderByCreatedAtAsc(user.getUserId())
                        .orElse(null);
                yield post != null ? post.getPostId() : null;
            }
            case COMMENT_CREATE -> {
                // 첫 번째 작성한 댓글이 달린 게시글
                Comment comment = commentRepository.findFirstByUser_UserIdOrderByCreatedAtAsc(user.getUserId())
                        .orElse(null);
                yield comment != null ? comment.getPost().getPostId() : null;
            }
            case MENTORING_COMPLETE -> {
                // TODO: 멘토링 도메인 구현 후 추가
                yield null;
            }
            default -> null;
        };
    }

    // 추억 상세 구성
    private PineconeMemoryResponse.MemoryDetail buildMemoryDetail(PineconeMemory memory) {
        return switch (memory.getSourceType()) {
            case MESSAGE -> {
                Message message = messageRepository.findById(memory.getSourceId()).orElse(null);
                if (message == null) {
                    yield null;
                }
                yield PineconeMemoryResponse.MemoryDetail.builder()
                        .memoryType("MESSAGE")
                        .sourceId(memory.getSourceId())
                        .memoryContent(message.getMessageTitle())
                        .relatedUserName(message.getReceiver().getName())
                        .createdAt(message.getCreatedAt().toString())
                        .build();
            }
            case POST -> {
                Post post = postRepository.findById(memory.getSourceId()).orElse(null);
                if (post == null) {
                    yield null;
                }
                yield PineconeMemoryResponse.MemoryDetail.builder()
                        .memoryType("POST")
                        .sourceId(memory.getSourceId())
                        .memoryContent(post.getPostTitle())
                        .relatedUserName(null)
                        .createdAt(post.getCreatedAt().toString())
                        .build();
            }
            case COMMENT -> {
                // sourceId는 post_id (댓글이 달린 게시글)
                Post post = postRepository.findById(memory.getSourceId()).orElse(null);
                if (post == null) {
                    yield null;
                }
                yield PineconeMemoryResponse.MemoryDetail.builder()
                        .memoryType("COMMENT")
                        .sourceId(memory.getSourceId())
                        .memoryContent(post.getPostTitle())
                        .relatedUserName(post.getAuthor().getName())
                        .createdAt(post.getCreatedAt().toString())
                        .build();
            }
            case MENTORING -> {
                // TODO: 멘토링 도메인 구현 후 추가
                yield null;
            }
        };
    }

    // 추억 상세 조회 가능 여부
    private boolean hasMemoryDetail(PineconeSourceType sourceType) {
        // MESSAGE, POST, COMMENT, MENTORING만 추억 상세 있음
        return sourceType != null;
    }

    // ConditionType으로 미션 제목 조회
    private String getMissionTitleByConditionType(MissionConditionType conditionType) {
        Mission mission = missionRepository.findAll().stream()
                .filter(m -> m.getConditionType() == conditionType)
                .findFirst()
                .orElse(null);
        return mission != null ? mission.getMissionTitle() : "";
    }

    // Memory에서 ConditionType 추출
//    private MissionConditionType getMissionConditionTypeFromMemory(PineconeMemory memory) {
//        return switch (memory.getSourceType()) {
//            case MESSAGE -> MissionConditionType.MESSAGE;
//            case POST -> MissionConditionType.POST_CREATE;
//            case COMMENT -> MissionConditionType.COMMENT_CREATE;
//            case MENTORING -> MissionConditionType.MENTORING_COMPLETE;
//        };
//    }

    // 회원가입 완료 후 미션 초기화
    @Transactional
    public void initializeUserMissions(User user) {
        List<Mission> allMissions = missionRepository.findAllByOrderByMissionCategoryAscSequenceOrderAsc();

        for (Mission mission : allMissions) {
            UserMission userMission = UserMission.builder()
                    .user(user)
                    .mission(mission)
                    .build();
            userMissionRepository.save(userMission);
        }

        log.info("사용자 미션 초기화 완료 - userId: {}, 미션 개수: {}", user.getUserId(), allMissions.size());
    }

    // 장학 프로그램 최신 3개 구성
    private List<MissionProgressResponse.ScholarshipProgramCard> buildScholarshipPrograms() {
        // 최신 3개 조회
        List<ScholarshipProgramPost> posts = scholarshipProgramPostRepository
                .findTop3WithPostAndAuthor(PageRequest.of(0, 3));

        if (posts.isEmpty()) {
            return List.of();
        }

        // postId 수집 (썸네일 이미지 조회용)
        List<Long> postIds = posts.stream()
                .map(spp -> spp.getPost().getPostId())
                .toList();

        // N+1 해결: 썸네일 이미지 배치 조회
        Map<Long, String> thumbnailUrlMap = fileAttachmentRepository
                .findByFileTargetTypeAndFileTargetIdInAndSortOrderAndPurpose(
                        FileTargetType.POST,
                        postIds,
                        1,
                        AttachmentPurpose.POST_ATTACHMENT
                )
                .stream()
                .collect(Collectors.toMap(
                        FileAttachment::getFileTargetId,
                        attachment -> attachment.getFile().getUrl(region),
                        (existing, replacement) -> existing
                ));

        // ScholarshipProgramCard 생성
        return posts.stream()
                .map(spp -> {
                    Post post = spp.getPost();
                    PostCategory category = post.getPostCategory();

                    return MissionProgressResponse.ScholarshipProgramCard.builder()
                            .postId(post.getPostId())
                            .category(category != null ? category.name() : null)
                            .categoryName(category != null ? category.getDescription() : null)
                            .title(post.getPostTitle())
                            .thumbnailUrl(thumbnailUrlMap.get(post.getPostId()))
                            .createdAt(post.getCreatedAt().toString())
                            .build();
                })
                .toList();
    }
}