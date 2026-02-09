package com.solif.backend.domain.mentoring.service;

import com.solif.backend.domain.auth.code.AuthErrorCode;
import com.solif.backend.domain.file.entity.AttachmentPurpose;
import com.solif.backend.domain.file.entity.FileAttachment;
import com.solif.backend.domain.file.entity.FileTargetType;
import com.solif.backend.domain.file.repository.FileAttachmentRepository;
import com.solif.backend.domain.file.service.FileService;
import com.solif.backend.domain.interest.entity.UserInterest;
import com.solif.backend.domain.interest.repository.UserInterestRepository;
import com.solif.backend.domain.mentoring.code.MentoringErrorCode;
import com.solif.backend.domain.mentoring.dto.*;
import com.solif.backend.domain.mentoring.entity.*;
import com.solif.backend.domain.mentoring.repository.MentorRepository;
import com.solif.backend.domain.mentoring.repository.MentoringCardRepository;
import com.solif.backend.domain.mentoring.repository.MentoringInteractionRepository;
import com.solif.backend.domain.mentoring.repository.MentoringRequestRepository;
import com.solif.backend.domain.post.entity.Post;
import com.solif.backend.domain.post.repository.PostRepository;
import com.solif.backend.domain.profile.entity.UserProfile;
import com.solif.backend.domain.profile.repository.UserProfileRepository;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MentoringService {

    private final MentorRepository mentorRepository;
    private final MentoringRequestRepository mentoringRequestRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserInterestRepository userInterestRepository;
    private final PostRepository postRepository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final MentoringCardRepository mentoringCardRepository;
    private final FileService fileService;
    private final MentoringInteractionRepository mentoringInteractionRepository;

    @Value("${cloud.aws.region.static}")
    private String region;

    // 전문가 멘토 목록 조회
    public List<MentorListResponse> getMentors() {
        log.info("전문가 멘토 목록 조회");

        List<Mentor> mentors = mentorRepository.findByIsActiveTrueOrderByCreatedAtDesc();

        return mentors.stream()
                .map(mentor -> MentorListResponse.from(
                        mentor,
                        mentor.getProfileImageFile()
                ))
                .collect(Collectors.toList());
    }

    // 멘토링 신청
    @Transactional
    public MentoringRequestCreateResponse createMentoringRequest(Long userId, MentoringRequestCreateRequest request) {
        log.info("멘토링 신청 - userId: {}, mentorId: {}", userId, request.getMentorId());

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 멘토 조회
        Mentor mentor = mentorRepository.findById(request.getMentorId())
                .orElseThrow(() -> new CustomException(MentoringErrorCode.MENTOR_NOT_FOUND));

        // Enum 변환
        MentoringCategory category = request.getCategory();
        MentoringMethod method = request.getMethod();

        // 멘토링 신청 생성
        MentoringRequest mentoringRequest = MentoringRequest.builder()
                .mentor(mentor)
                .menteeUser(user)
                .category(category)
                .content(request.getContent())
                .method(method)
                .build();

        MentoringRequest saved = mentoringRequestRepository.save(mentoringRequest);

        return MentoringRequestCreateResponse.from(saved);
    }

    // 내가 보낸 신청서 목록 조회
    public Slice<MentoringRequestListResponse> getSentRequests(Long userId, Pageable pageable) {
        log.info("내가 보낸 신청서 목록 조회 - userId: {}", userId);

        Slice<MentoringRequest> requests = mentoringRequestRepository.findSentRequests(userId, pageable);

        return requests.map(MentoringRequestListResponse::from);
    }

    // 멘토링 신청서 상세 조회
    public MentoringRequestDetailResponse getRequestDetail(Long userId, Long requestId) {
        log.info("멘토링 신청서 상세 조회 - userId: {}, requestId: {}", userId, requestId);

        // 신청서 조회
        MentoringRequest request = mentoringRequestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new CustomException(MentoringErrorCode.MENTORING_REQUEST_NOT_FOUND));

        // 권한 확인 (신청자 본인만 조회 가능)
        if (!request.isMentee(userId)) {
            throw new CustomException(MentoringErrorCode.UNAUTHORIZED_REQUEST_ACCESS);
        }

        // Mentor Entity에서 직접 profileImageFile 가져오기
        String mentorProfileImageUrl = request.getMentor().getProfileImageFile();

        return MentoringRequestDetailResponse.from(request, mentorProfileImageUrl);
    }

    // 내가 받은 답변 목록 조회
    public Slice<MentoringRequestListResponse> getReceivedRequests(Long userId, Pageable pageable) {
        log.info("받은 신청서 목록 조회 - userId: {}", userId);

        Slice<MentoringRequest> requests = mentoringRequestRepository.findReceivedRequests(userId, pageable);

        return requests.map(MentoringRequestListResponse::from);
    }

    // 멘토링 홈 조회
    public MentoringHomeResponse getMentoringHome(Long userId) {
        log.info("멘토링 홈 조회 - userId: {}", userId);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 1. 전문가 멘토 목록 조회 (카테고리별)
        List<Mentor> allMentors = mentorRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        List<Mentor> studyMentors = mentorRepository.findByIsActiveTrueAndMentorCategoryOrderByCreatedAtDesc(MentorCategory.STUDY);
        List<Mentor> jobMentors = mentorRepository.findByIsActiveTrueAndMentorCategoryOrderByCreatedAtDesc(MentorCategory.JOB);
        List<Mentor> lifeMentors = mentorRepository.findByIsActiveTrueAndMentorCategoryOrderByCreatedAtDesc(MentorCategory.LIFE);

        List<MentorListResponse> allMentorResponses = allMentors.stream()
                .map(mentor -> MentorListResponse.from(mentor, mentor.getProfileImageFile()))
                .collect(Collectors.toList());

        List<MentorListResponse> studyMentorResponses = studyMentors.stream()
                .map(mentor -> MentorListResponse.from(mentor, mentor.getProfileImageFile()))
                .collect(Collectors.toList());

        List<MentorListResponse> jobMentorResponses = jobMentors.stream()
                .map(mentor -> MentorListResponse.from(mentor, mentor.getProfileImageFile()))
                .collect(Collectors.toList());

        List<MentorListResponse> lifeMentorResponses = lifeMentors.stream()
                .map(mentor -> MentorListResponse.from(mentor, mentor.getProfileImageFile()))
                .collect(Collectors.toList());

        // 2. 선후배 멘토링 - 응원하기 (통합 메서드 사용)
        MentoringHomeResponse.SeniorJuniorMentoringResponse cheerList =
                buildInteractionList(currentUser, MentoringInteractionType.CHEER);

        // 3. 선후배 멘토링 - 경험나누기 (통합 메서드 사용)
        MentoringHomeResponse.SeniorJuniorMentoringResponse helpList =
                buildInteractionList(currentUser, MentoringInteractionType.HELP);

        // 4. 멘토링 후기 목록 (boardId=2, 최대 4개)
        Pageable topReviews = PageRequest.of(0, 4);
        Slice<Post> reviewPosts = postRepository.findByBoard_BoardIdAndDeletedAtIsNullWithAuthor(2L, topReviews);

        List<MentoringHomeResponse.MentoringReviewResponse> reviewResponses = reviewPosts.stream()
                .map(post -> MentoringHomeResponse.MentoringReviewResponse.builder()
                        .postId(post.getPostId())
                        .title(post.getPostTitle())
                        .authorName(post.getAuthor().getName())
                        .category(post.getPostCategory() != null ? post.getPostCategory().name() : null)
                        .viewCount(post.getViewCount())
                        .build())
                .collect(Collectors.toList());

        return MentoringHomeResponse.builder()
                .allMentors(allMentorResponses)
                .studyMentors(studyMentorResponses)
                .jobMentors(jobMentorResponses)
                .lifeMentors(lifeMentorResponses)
                .cheerList(cheerList)
                .helpList(helpList)
                .reviews(reviewResponses)
                .build();
    }

    // 멘토링 엽서 발송
    @Transactional
    public MentoringCardSendResponse sendMentoringCard(Long userId, MentoringCardSendRequest request) {
        log.info("멘토링 엽서 발송 - userId: {}", userId);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // Enum 변환
        MentoringCategory category = request.getCategory();
        MentoringMethod method = request.getMethod();

        // 멘토링 엽서 생성
        MentoringCard card = MentoringCard.builder()
                .sender(user)
                .cardTitle(request.getCardTitle())
                .category(category)
                .method(method)
                .cardContent(request.getCardContent())
                .build();

        MentoringCard savedCard = mentoringCardRepository.save(card);

        // 파일 확정 (fileIds가 있으면)
        if (request.getFileIds() != null && !request.getFileIds().isEmpty()) {
            fileService.confirmFiles(
                    request.getFileIds(),
                    FileTargetType.MENTORING_CARD,
                    savedCard.getMentoringCardId(),
                    AttachmentPurpose.MENTORING_CARD_ATTACHMENT
            );
        }

        return MentoringCardSendResponse.from(savedCard);
    }

    // 보낸 멘토링 엽서 목록 조회
    public Slice<MentoringCardListResponse> getSentCards(Long userId, Pageable pageable) {
        log.info("보낸 멘토링 엽서 목록 조회 - userId: {}", userId);

        Slice<MentoringCard> cards = mentoringCardRepository.findSentCards(userId, pageable);

        return cards.map(MentoringCardListResponse::from);
    }

    // 멘토링 엽서 상세 조회
    @Transactional(readOnly = true)
    public MentoringCardDetailResponse getCardDetail(Long userId, Long cardId) {
        log.info("멘토링 엽서 상세 조회 - userId: {}, cardId: {}", userId, cardId);

        MentoringCard card = mentoringCardRepository.findById(cardId)
                .orElseThrow(() -> new CustomException(MentoringErrorCode.MENTORING_CARD_NOT_FOUND));

        // 권한 확인 (본인만 조회 가능)
        if (!card.isSender(userId)) {
            throw new CustomException(MentoringErrorCode.UNAUTHORIZED_CARD_ACCESS);
        }

        // 첨부 파일 URL 조회
        List<FileAttachment> attachments = fileAttachmentRepository
                .findByFileTargetTypeAndTargetIdOrderBySortOrder(FileTargetType.MENTORING_CARD, cardId);

        List<String> imageUrls = attachments.stream()
                .map(attachment -> attachment.getFile().getUrl(region))
                .toList();

        return MentoringCardDetailResponse.from(card, imageUrls);
    }

    // === Helper Methods ===

    // 통합: buildCheerList + buildHelpList → buildInteractionList
    private MentoringHomeResponse.SeniorJuniorMentoringResponse buildInteractionList(
            User currentUser,
            MentoringInteractionType type) {

        // MASTER(관리자)는 상호작용 대상 없음
        if (currentUser.getUserRole() == User.UserRole.MASTER) {
            return MentoringHomeResponse.SeniorJuniorMentoringResponse.builder()
                    .users(List.of())
                    .build();
        }

        List<MentoringHomeResponse.SeniorJuniorMentoringResponse.UserCardResponse> userCards = new ArrayList<>();

        // 1) 내가 받은 상호작용 중 아직 내가 보내지 않은 사람들 (대기중)
        List<MentoringInteraction> pendingInteractions = mentoringInteractionRepository
                .findPendingReceivedInteractions(currentUser.getUserId(), type);

        // 2) 양방향 완료된 상호작용 (서로 보낸 사람들)
        List<MentoringInteraction> completedInteractions = mentoringInteractionRepository
                .findCompletedInteractions(currentUser.getUserId(), type);

        // 모든 대상 사용자 ID 수집
        Set<Long> allUserIds = new HashSet<>();

        for (MentoringInteraction interaction : pendingInteractions) {
            allUserIds.add(interaction.getSender().getUserId());
        }

        for (MentoringInteraction interaction : completedInteractions) {
            User otherUser = interaction.getSender().getUserId().equals(currentUser.getUserId())
                    ? interaction.getReceiver()
                    : interaction.getSender();
            allUserIds.add(otherUser.getUserId());
        }

        // 최대 10명으로 제한
        List<Long> limitedUserIds = allUserIds.stream().limit(10).collect(Collectors.toList());

        if (limitedUserIds.isEmpty()) {
            return MentoringHomeResponse.SeniorJuniorMentoringResponse.builder()
                    .users(List.of())
                    .build();
        }

        // 배치 조회 (N+1 방지)
        Map<Long, UserProfile> profileMap = userProfileRepository.findByUser_UserIdIn(limitedUserIds).stream()
                .collect(Collectors.toMap(
                        profile -> profile.getUser().getUserId(),
                        profile -> profile
                ));

        Map<Long, List<String>> interestMap = userInterestRepository.findAllByUser_UserIdIn(limitedUserIds).stream()
                .collect(Collectors.groupingBy(
                        interest -> interest.getUser().getUserId(),
                        Collectors.mapping(UserInterest::getCategoryName, Collectors.toList())
                ));

        // pending 처리
        for (MentoringInteraction interaction : pendingInteractions) {
            User sender = interaction.getSender();
            if (limitedUserIds.contains(sender.getUserId())) {
                userCards.add(buildUserCardFromCache(sender, "PENDING", profileMap, interestMap));
            }
        }

        // completed 처리
        Set<Long> addedUserIds = userCards.stream()
                .map(MentoringHomeResponse.SeniorJuniorMentoringResponse.UserCardResponse::getUserId)
                .collect(Collectors.toSet());

        for (MentoringInteraction interaction : completedInteractions) {
            User otherUser = interaction.getSender().getUserId().equals(currentUser.getUserId())
                    ? interaction.getReceiver()
                    : interaction.getSender();

            if (limitedUserIds.contains(otherUser.getUserId()) && !addedUserIds.contains(otherUser.getUserId())) {
                userCards.add(buildUserCardFromCache(otherUser, "COMPLETED", profileMap, interestMap));
                addedUserIds.add(otherUser.getUserId());
            }
        }

        return MentoringHomeResponse.SeniorJuniorMentoringResponse.builder()
                .users(userCards)
                .build();
    }

    // UserCard 생성 헬퍼 메서드
    private MentoringHomeResponse.SeniorJuniorMentoringResponse.UserCardResponse buildUserCardFromCache(
            User user,
            String status,
            Map<Long, UserProfile> profileMap,
            Map<Long, List<String>> interestMap) {

        UserProfile profile = profileMap.get(user.getUserId());
        List<String> interests = interestMap.getOrDefault(user.getUserId(), List.of());

        return MentoringHomeResponse.SeniorJuniorMentoringResponse.UserCardResponse.builder()
                .userId(user.getUserId())
                .userName(user.getName())
                .character(profile != null ? profile.getUserCharacter() : null)
                .backgroundPattern(profile != null ? profile.getBackgroundPattern() : null)
                .solidGoalName(profile != null ? profile.getSolidGoalName() : null)
                .interests(interests)
                .status(status)
                .build();
    }
}