package com.solif.backend.domain.network.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solif.backend.domain.council.entity.CouncilMember;
import com.solif.backend.domain.council.repository.CouncilMemberRepository;
import com.solif.backend.domain.interest.entity.UserInterest;
import com.solif.backend.domain.interest.repository.UserInterestRepository;
import com.solif.backend.domain.network.dto.*;
import com.solif.backend.domain.network.entity.Connection;
import com.solif.backend.domain.network.entity.ConnectionStatus;
import com.solif.backend.domain.network.exception.NetworkErrorCode;
import com.solif.backend.domain.network.repository.ConnectionRepository;
import com.solif.backend.domain.notification.entity.Notification;
import com.solif.backend.domain.notification.entity.NotificationType;
import com.solif.backend.domain.notification.entity.TargetType;
import com.solif.backend.domain.notification.repository.NotificationRepository;
import com.solif.backend.domain.profile.entity.UserProfile;
import com.solif.backend.domain.profile.repository.UserProfileRepository;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NetworkService {

    private final ConnectionRepository connectionRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserInterestRepository userInterestRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final ObjectMapper objectMapper;

    // 나의 교류망 목록 조회
    public NetworkListResponse getMyNetworks(Long userId) {
        User user = findUserById(userId);

        List<Connection> connections =
                connectionRepository.findAllByRegisterAndStatusOrTargetAndStatus(
                        user, ConnectionStatus.ACCEPTED,
                        user, ConnectionStatus.ACCEPTED
                );

        List<NetworkListResponse.FriendSummary> addedFriends = connections.stream()
                .map(conn -> {
                    User target = conn.getTarget();
                    UserProfile profile = findProfileByUser(target);
                    return NetworkListResponse.FriendSummary.builder()
                            .userId(target.getUserId())
                            .userName(target.getName())
                            .character(profile.getUserCharacter())
                            .backgroundPattern(profile.getBackgroundPattern())
                            .build();
                })
                .collect(Collectors.toList());

        // 자치회 정보 배치 조회 (N+1 방지)
        List<Long> targetUserIds = connections.stream()
                .map(conn -> conn.getTarget().getUserId())
                .collect(Collectors.toList());
        Map<Long, CouncilMember> membershipMap = getCouncilMembershipMap(targetUserIds);

        List<NetworkListResponse.NetworkCard> networkCards = connections.stream()
                .map(conn -> {
                    User target = conn.getTarget();
                    UserProfile profile = findProfileByUser(target);
                    List<String> interests = getInterestsByUser(target);
                    List<String> mainGoals = convertJsonToList(profile.getMainGoal());
                    String buttonType = determineButtonType(user, target);

                    // 자치회 정보 추가
                    CouncilMember membership = membershipMap.get(target.getUserId());

                    return NetworkListResponse.NetworkCard.builder()
                            .userId(target.getUserId())
                            .userName(target.getName())
                            .character(profile.getUserCharacter())
                            .backgroundPattern(profile.getBackgroundPattern())
                            .solidGoalName(profile.getSolidGoalName())
                            .mainGoals(mainGoals)
                            .interests(interests)
                            .buttonType(buttonType)
                            .isInCouncil(membership != null)
                            .councilName(membership != null ? membership.getCouncil().getCouncilName() : null)
                            .build();
                })
                .collect(Collectors.toList());

        return NetworkListResponse.builder()
                .myRole(user.getUserRole().name())
                .totalCount(connections.size())
                .addedFriends(addedFriends)
                .networkCards(networkCards)
                .build();
    }

    // 교류망 추가 (사용자 ID로)
    @Transactional
    public NetworkAddResponse addNetwork(Long userId, NetworkAddRequest request) {
        User user = findUserById(userId);
        User targetUser;

        if (request.getTargetUserId() != null) {
            targetUser = findUserById(request.getTargetUserId());
        } else {
            throw new CustomException(NetworkErrorCode.INVALID_QR_CODE);
        }

        return createConnection(user, targetUser);
    }

    // QR 코드 스캔으로 교류망 추가
    @Transactional
    public NetworkAddResponse addNetworkByQrScan(Long userId, String qrData) {
        User scanner = findUserById(userId);
        User targetUser = findUserByQrCode(qrData);

        // 교류망 생성
        NetworkAddResponse response = createConnection(scanner, targetUser);

        // 알림 발송: QR이 찍힌 사용자(targetUser)에게 알림
        Notification notification = Notification.builder()
                .receiver(targetUser)
                .notificationType(NotificationType.CONNECTION)
                .targetType(TargetType.NETWORK)
                .targetId(scanner.getUserId())
                .notificationTitle("교류망에 추가되었어요!")
                .notificationContent(scanner.getName() + "님이 교류망에 나를 추가했어요")
                .build();
        notificationRepository.save(notification);

        return response;
    }

    // 교류망 생성 공통 로직
    private NetworkAddResponse createConnection(User register, User target) {
        // 자기 자신 추가 방지
        if (register.getUserId().equals(target.getUserId())) {
            throw new CustomException(NetworkErrorCode.SELF_CONNECTION_NOT_ALLOWED);
        }

        // 이미 연결되어 있는지 확인
        if (connectionRepository.existsByRegisterAndTargetOrTargetAndRegister(
                register, target, target, register
        )) {
            throw new CustomException(NetworkErrorCode.CONNECTION_ALREADY_EXISTS);
        }

        // 연결 생성 (바로 ACCEPTED)
        Connection connection = Connection.builder()
                .register(register)
                .target(target)
                .status(ConnectionStatus.ACCEPTED)
                .build();
        connectionRepository.save(connection);

        // QR을 찍은 사람(register)의 connectionCount만 증가
        UserProfile registerProfile = findProfileByUser(register);
        registerProfile.incrementConnectionCount();

        return NetworkAddResponse.builder()
                .connectionId(connection.getConnectionId())
                .targetUserId(target.getUserId())
                .targetUserName(target.getName())
                .createdAt(connection.getCreatedAt())
                .build();
    }

    // 상호작용 발송 (알림 생성)
    @Transactional
    public NetworkInteractionResponse sendInteraction(Long userId, NetworkInteractionRequest request) {
        User sender = findUserById(userId);
        User receiver = findUserById(request.getTargetUserId());

        NotificationType notificationType;
        String title;
        String content;

        try {
            notificationType = NotificationType.valueOf(request.getInteractionType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(NetworkErrorCode.INVALID_INTERACTION_TYPE);
        }

        // 역할 기반 상호작용 타입 검증
        validateInteractionType(sender, receiver, notificationType);

        if (notificationType == NotificationType.CHEER) {
            title = "응원이 도착했어요!";
            content = sender.getName() + "님이 응원을 보냈어요";
        } else if (notificationType == NotificationType.HELP) {
            title = "경험 나누기 요청이 도착했어요!";
            content = sender.getName() + "님이 경험 나누기를 요청했어요";
        } else {
            throw new CustomException(NetworkErrorCode.INVALID_INTERACTION_TYPE);
        }

        Notification notification = Notification.builder()
                .receiver(receiver)
                .notificationType(notificationType)
                .targetType(TargetType.NETWORK)
                .targetId(sender.getUserId())
                .notificationTitle(title)
                .notificationContent(content)
                .build();

        notificationRepository.save(notification);

        return NetworkInteractionResponse.builder()
                .notificationId(notification.getNotificationId())
                .targetUserId(receiver.getUserId())
                .interactionType(notificationType.name())
                .notificationContent(content)
                .createdAt(notification.getCreatedAt())
                .build();
    }

    // 교류망 추천 조회
    public NetworkRecommendationResponse getRecommendations(Long userId) {
        User user = findUserById(userId);
        List<String> myInterests = getInterestsByUser(user);

        // 나와 같은 관심사를 가진 사용자 조회
        List<NetworkRecommendationResponse.RecommendedUser> interestBasedUsers =
                findUsersWithSameInterests(user, myInterests);

        // 전체 사용자 둘러보기 (본인 제외)
        List<NetworkRecommendationResponse.RecommendedUser> allUsersList =
                findAllUsersExcept(user);

        return NetworkRecommendationResponse.builder()
                .interestBased(NetworkRecommendationResponse.RecommendationGroup.builder()
                        .title("나와 같은 관심사를 가지고 있어요")
                        .users(interestBasedUsers)
                        .build())
                .allUsers(NetworkRecommendationResponse.RecommendationGroup.builder()
                        .title("다른 사람 둘러보기")
                        .users(allUsersList)
                        .build())
                .build();
    }

    // 교류망 검색
    public NetworkSearchResponse searchNetworks(Long userId, String keyword) {
        User user = findUserById(userId);

        List<User> searchedUsers = userRepository.findByNameContaining(keyword);

        // 자치회 정보 배치 조회 (N+1 방지)
        List<Long> userIds = searchedUsers.stream()
                .map(User::getUserId)
                .filter(id -> !id.equals(userId))
                .collect(Collectors.toList());
        Map<Long, CouncilMember> membershipMap = getCouncilMembershipMap(userIds);

        List<NetworkSearchResponse.SearchedUser> users = searchedUsers.stream()
                .filter(u -> !u.getUserId().equals(userId))
                .map(u -> {
                    UserProfile profile = userProfileRepository.findByUser_UserId(u.getUserId()).orElse(null);
                    List<String> interests = getInterestsByUser(u);
                    boolean isConnected = connectionRepository.existsByRegisterAndTarget(user, u);

                    // 자치회 정보 추가
                    CouncilMember membership = membershipMap.get(u.getUserId());

                    return NetworkSearchResponse.SearchedUser.builder()
                            .userId(u.getUserId())
                            .userName(u.getName())
                            .character(profile != null ? profile.getUserCharacter() : null)
                            .backgroundPattern(profile != null ? profile.getBackgroundPattern() : null)
                            .solidGoalName(profile != null ? profile.getSolidGoalName() : null)
                            .interests(interests)
                            .isConnected(isConnected)
                            .isInCouncil(membership != null)
                            .councilName(membership != null ? membership.getCouncil().getCouncilName() : null)
                            .build();
                })
                .collect(Collectors.toList());

        return NetworkSearchResponse.builder()
                .keyword(keyword)
                .resultCount(users.size())
                .users(users)
                .build();
    }

    // ===== Helper Methods =====

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(NetworkErrorCode.USER_NOT_FOUND));
    }

    private UserProfile findProfileByUser(User user) {
        return userProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new CustomException(NetworkErrorCode.PROFILE_NOT_FOUND));
    }

    private User findUserByQrCode(String qrCode) {
        return userProfileRepository.findByQrCodeData(qrCode)
                .map(UserProfile::getUser)
                .orElseThrow(() -> new CustomException(NetworkErrorCode.INVALID_QR_CODE));
    }

    private List<String> getInterestsByUser(User user) {
        return userInterestRepository.findAllByUser_UserId(user.getUserId())
                .stream()
                .map(UserInterest::getCategoryName)
                .collect(Collectors.toList());
    }

    // 자치회 멤버십 배치 조회 (N+1 방지)
    private Map<Long, CouncilMember> getCouncilMembershipMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        Set<Long> userIdSet = new HashSet<>(userIds);
        return councilMemberRepository.findByUserIdIn(userIdSet).stream()
                .collect(Collectors.toMap(
                        cm -> cm.getUser().getUserId(),
                        Function.identity()
                ));
    }

    private String determineButtonType(User me, User target) {
        // 내가 상대보다 윗 역할이면 CHEER(응원하기)
        // 동일 역할이거나 아랫 역할이면 HELP(경험나누기)
        int myLevel = getRoleLevel(me.getUserRole());
        int targetLevel = getRoleLevel(target.getUserRole());

        if (myLevel > targetLevel) {
            return "CHEER";
        }
        return "HELP";
    }

    private int getRoleLevel(User.UserRole role) {
        return switch (role) {
            case JUNIOR -> 1;
            case SENIOR -> 2;
            case GRADUATE -> 3;
            case MASTER -> 4;
        };
    }

    private void validateInteractionType(User sender, User receiver, NotificationType type) {
        int senderLevel = getRoleLevel(sender.getUserRole());
        int receiverLevel = getRoleLevel(receiver.getUserRole());

        // CHEER는 보내는 사람이 받는 사람보다 윗 역할일 때만 가능
        if (type == NotificationType.CHEER && senderLevel <= receiverLevel) {
            throw new CustomException(NetworkErrorCode.INTERACTION_NOT_ALLOWED);
        }

        // HELP는 보내는 사람이 받는 사람과 같거나 아래 역할일 때만 가능
        if (type == NotificationType.HELP && senderLevel > receiverLevel) {
            throw new CustomException(NetworkErrorCode.INTERACTION_NOT_ALLOWED);
        }
    }

    private List<NetworkRecommendationResponse.RecommendedUser> findUsersWithSameInterests(User user, List<String> myInterests) {
        if (myInterests.isEmpty()) {
            return List.of();
        }

        List<User> users = userInterestRepository.findAllByCategoryNameIn(myInterests).stream()
                .map(UserInterest::getUser)
                .filter(u -> !u.getUserId().equals(user.getUserId()))
                .distinct()
                .limit(10)
                .collect(Collectors.toList());

        // 자치회 정보 배치 조회 (N+1 방지)
        List<Long> userIds = users.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
        Map<Long, CouncilMember> membershipMap = getCouncilMembershipMap(userIds);

        return users.stream()
                .map(u -> {
                    UserProfile profile = userProfileRepository.findByUser_UserId(u.getUserId()).orElse(null);
                    List<String> interests = getInterestsByUser(u);
                    CouncilMember membership = membershipMap.get(u.getUserId());

                    return NetworkRecommendationResponse.RecommendedUser.builder()
                            .userId(u.getUserId())
                            .userName(u.getName())
                            .character(profile != null ? profile.getUserCharacter() : null)
                            .backgroundPattern(profile != null ? profile.getBackgroundPattern() : null)
                            .solidGoalName(profile != null ? profile.getSolidGoalName() : null)
                            .interests(interests)
                            .isInCouncil(membership != null)
                            .councilName(membership != null ? membership.getCouncil().getCouncilName() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<NetworkRecommendationResponse.RecommendedUser> findAllUsersExcept(User user) {
        List<User> users = userRepository.findAll().stream()
                .filter(u -> !u.getUserId().equals(user.getUserId()))
                .limit(20)
                .collect(Collectors.toList());

        // 자치회 정보 배치 조회 (N+1 방지)
        List<Long> userIds = users.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
        Map<Long, CouncilMember> membershipMap = getCouncilMembershipMap(userIds);

        return users.stream()
                .map(u -> {
                    UserProfile profile = userProfileRepository.findByUser_UserId(u.getUserId()).orElse(null);
                    List<String> interests = getInterestsByUser(u);
                    CouncilMember membership = membershipMap.get(u.getUserId());

                    return NetworkRecommendationResponse.RecommendedUser.builder()
                            .userId(u.getUserId())
                            .userName(u.getName())
                            .character(profile != null ? profile.getUserCharacter() : null)
                            .backgroundPattern(profile != null ? profile.getBackgroundPattern() : null)
                            .solidGoalName(profile != null ? profile.getSolidGoalName() : null)
                            .interests(interests)
                            .isInCouncil(membership != null)
                            .councilName(membership != null ? membership.getCouncil().getCouncilName() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<String> convertJsonToList(String json) {
        if (json == null) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}