package com.solif.backend.domain.post.service;

import com.solif.backend.domain.mentoringpost.entity.MentoringPost;
import com.solif.backend.domain.mentoringpost.repository.MentoringPostRepository;
import com.solif.backend.domain.counselingpost.entity.CounselingPost;
import com.solif.backend.domain.counselingpost.repository.CounselingPostRepository;
import com.solif.backend.domain.mission.entity.MissionConditionType;
import com.solif.backend.domain.mission.service.MissionService;
import com.solif.backend.domain.noticepost.entity.NoticePost;
import com.solif.backend.domain.noticepost.repository.NoticePostRepository;
import com.solif.backend.domain.auth.code.AuthErrorCode;
import com.solif.backend.domain.board.code.BoardErrorCode;
import com.solif.backend.domain.board.entity.Board;
import com.solif.backend.domain.board.repository.BoardRepository;
import com.solif.backend.domain.comment.dto.PostCommentCount;
import com.solif.backend.domain.comment.repository.CommentRepository;
import com.solif.backend.domain.councilreview.entity.CouncilReviewPost;
import com.solif.backend.domain.councilreview.repository.CouncilReviewPostRepository;
import com.solif.backend.domain.file.entity.AttachmentPurpose;
import com.solif.backend.domain.file.entity.FileAttachment;
import com.solif.backend.domain.file.entity.FileTargetType;
import com.solif.backend.domain.file.repository.FileAttachmentRepository;
import com.solif.backend.domain.file.service.FileService;
import com.solif.backend.domain.post.dto.*;
import com.solif.backend.domain.post.entity.Post;
import com.solif.backend.domain.post.entity.PostCategory;
import com.solif.backend.domain.post.code.PostErrorCode;
import com.solif.backend.domain.post.repository.PostRepository;
import com.solif.backend.domain.postlike.repository.PostLikeRepository;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final CouncilReviewPostRepository councilReviewPostRepository;
    private final FileService fileService;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final MentoringPostRepository mentoringPostRepository;
    private final CounselingPostRepository counselingPostRepository;
    private final NoticePostRepository noticePostRepository;
    private final MissionService missionService;

    @Value("${cloud.aws.region.static}")
    private String region;

    // 게시글 목록 조회
    public Slice<PostListResponse> getPosts(Long boardId, PostCategory category, Pageable pageable) {
        log.info("게시글 목록 조회 - boardId: {}, category: {}", boardId, category);

        // Board 존재 확인
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(BoardErrorCode.BOARD_NOT_FOUND));

        // 카테고리 있으면 카테고리별 조회, 없으면 전체 조회
        Slice<Post> posts;

        // boardId=1 (자치회 활동 후기) - category 없음
        if (boardId == 1L) {
            // 자치회는 전체만 (카테고리 없음)
            posts = postRepository.findByBoard_BoardIdAndDeletedAtIsNullWithAuthor(boardId, pageable);
        }
        // boardId=2 (멘토링 후기) - category 있음
        else if (boardId == 2L) {
            if (category != null) {
                posts = postRepository.findByBoard_BoardIdAndPostCategoryAndDeletedAtIsNullWithAuthor(boardId, category, pageable);
            } else {
                posts = postRepository.findByBoard_BoardIdAndDeletedAtIsNullWithAuthor(boardId, pageable);
            }
        }
        // boardId=3 (고민상담) - 익명, category 있음
        else if (boardId == 3L) {
            if (category != null) {
                posts = postRepository.findByBoard_BoardIdAndPostCategoryAndDeletedAtIsNull(boardId, category, pageable);
            } else {
                posts = postRepository.findByBoard_BoardIdAndDeletedAtIsNull(boardId, pageable);
            }
        }
        // boardId=4 (재단소식) - category 있음
        else {
            if (category != null) {
                posts = postRepository.findByBoard_BoardIdAndPostCategoryAndDeletedAtIsNullWithAuthor(boardId, category, pageable);
            } else {
                posts = postRepository.findByBoard_BoardIdAndDeletedAtIsNullWithAuthor(boardId, pageable);
            }
        }

        // 익명 여부 판단 (boardId=3만 익명)
        boolean isAnonymous = (boardId == 3L);

        // N+1 해결: 모든 postId를 모아서 한 번에 댓글 수 조회
        List<Long> postIds = posts.getContent()
                .stream()
                .map(Post::getPostId)
                .toList();

        // 빈 리스트 처리: postIds가 비어있으면 빈 Map 반환
        Map<Long, Long> commentCounts = postIds.isEmpty()
                ? Map.of()
                : commentRepository.countByPostIds(postIds)
                .stream()
                .collect(Collectors.toMap(
                        PostCommentCount::getPostId,
                        PostCommentCount::getCommentCount
                ));

        // N+1 해결: 대표 이미지를 배치로 조회
        Map<Long, String> thumbnailUrlMap = new HashMap<>();

        if (boardId == 1L) {
            // 자치회 활동 후기: councilReviewPostId 수집
            List<Long> councilReviewPostIds = posts.getContent().stream()
                    .map(Post::getPostId)
                    .map(postId -> councilReviewPostRepository.findByPostId(postId).orElse(null))
                    .filter(Objects::nonNull)
                    .map(CouncilReviewPost::getCouncilReviewPostId)
                    .toList();

            if (!councilReviewPostIds.isEmpty()) {
                thumbnailUrlMap = fileAttachmentRepository
                        .findByFileTargetTypeAndFileTargetIdInAndSortOrderAndPurpose(
                                FileTargetType.COUNCIL_POST,
                                councilReviewPostIds,
                                1,
                                AttachmentPurpose.POST_ATTACHMENT
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                FileAttachment::getFileTargetId,
                                attachment -> attachment.getFile().getUrl(region),
                                (existing, replacement) -> existing  // 중복 키 처리
                        ));
            }
        } else {
            // 통합 게시글: postId 수집
            if (!postIds.isEmpty()) {
                thumbnailUrlMap = fileAttachmentRepository
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
                                (existing, replacement) -> existing  // 중복 키 처리
                        ));
            }
        }

        // 최종 thumbnailUrlMap 변수를 람다에서 사용하기 위해 final로 선언
        final Map<Long, String> finalThumbnailUrlMap = thumbnailUrlMap;

        // Post -> PostListResponse 변환
        return posts.map(post -> {
            Long commentCount = commentCounts.getOrDefault(post.getPostId(), 0L);

            // 자치회 활동 후기인 경우 추가 정보 조회
            String councilName = null;
            String thumbnailImageUrl = null;

            if (boardId == 1L) { // 자치회 활동 후기
                CouncilReviewPost reviewPost = councilReviewPostRepository
                        .findByPostId(post.getPostId())
                        .orElse(null);

                if (reviewPost != null) {
                    councilName = reviewPost.getCouncil().getCouncilName();
                    thumbnailImageUrl = finalThumbnailUrlMap.get(reviewPost.getCouncilReviewPostId());
                }
            } else {
                // Map에서 조회 (O(1))
                thumbnailImageUrl = finalThumbnailUrlMap.get(post.getPostId());
            }

            return PostListResponse.from(post, commentCount, isAnonymous, councilName, thumbnailImageUrl);
        });
    }

    // 게시글 상세 조회
    @Transactional
    public PostDetailResponse getPostDetail(Long userId, Long postId) {
        log.info("게시글 상세 조회 - postId: {}", postId);

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

        // 삭제된 게시글 체크
        if (post.isDeleted()) {
            throw new CustomException(PostErrorCode.DELETED_POST);
        }

        // 조회수 증가
        post.increaseViewCount();

        // 미션 체크: 재단 소식(공지사항) 확인
        // boardId = 4 (재단소식) AND category = NOTICE (공지사항)
        if (post.getBoard().getBoardId() == 4L && post.getPostCategory() == PostCategory.NOTICE) {
            missionService.incrementMissionProgress(userId, MissionConditionType.POST_VIEW);
        }

        // 익명 여부 판단 (boardId=3만 익명)
        boolean isAnonymous = (post.getBoard().getBoardId() == 3L);

        // 댓글 수 조회
        Long commentCount = commentRepository.countByPost_PostId(postId);

        // 좋아요 수 조회
        Long likeCount = postLikeRepository.countByPost_PostId(postId);

        // 현재 사용자의 좋아요 여부 조회
        Boolean isLikedByUser = postLikeRepository.existsByUser_UserIdAndPost_PostId(userId, postId);

        // 첨부 이미지 조회
        List<FileAttachment> attachments = fileAttachmentRepository
                .findByFileTargetTypeAndFileTargetIdOrderBySortOrder(FileTargetType.POST, postId);

        List<String> imageUrls = attachments.stream()
                .map(attachment -> attachment.getFile().getUrl(region))
                .toList();

        return PostDetailResponse.from(post, commentCount, likeCount, isLikedByUser, isAnonymous, imageUrls);
    }

    // 게시글 작성
    @Transactional
    public PostCreateResponse createPost(Long userId, PostCreateRequest request) {
        log.info("게시글 작성 - userId: {}, boardId: {}", userId, request.getBoardId());

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 게시판 조회
        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(() -> new CustomException(BoardErrorCode.BOARD_NOT_FOUND));

        // 게시판별 카테고리 검증
        validateCategoryForBoard(request.getBoardId(), request.getPostCategory());

        // Post 엔티티 생성
        Post post = Post.builder()
                .author(user)
                .board(board)
                .postCategory(request.getPostCategory())
                .postTitle(request.getPostTitle())
                .postContent(request.getPostContent())
                .build();

        // 저장
        Post savedPost = postRepository.save(post);

        // 게시판별 타입 테이블 생성
        if (request.getBoardId() == 1L) {
            // 자치회 활동 후기는 CouncilReviewPostController 사용
            throw new CustomException(PostErrorCode.INVALID_BOARD);
        } else if (request.getBoardId() == 2L) {
            // 멘토링 후기
            MentoringPost mentoringPost = MentoringPost.builder()
                    .post(savedPost)
                    .build();
            mentoringPostRepository.save(mentoringPost);
            log.info("멘토링 후기 생성 - mentoringPostId: {}", mentoringPost.getMentoringPostId());
        } else if (request.getBoardId() == 3L) {
            // 고민상담
            CounselingPost counselingPost = CounselingPost.builder()
                    .post(savedPost)
                    .build();
            counselingPostRepository.save(counselingPost);
            log.info("고민상담 생성 - counselingPostId: {}", counselingPost.getCounselingPostId());
        } else if (request.getBoardId() == 4L) {
            // 재단소식
            NoticePost noticePost = NoticePost.builder()
                    .post(savedPost)
                    .build();
            noticePostRepository.save(noticePost);
            log.info("재단소식 생성 - noticePostId: {}", noticePost.getNoticePostId());
        }

        // 파일 확정 (fileIds가 있으면)
        if (request.getFileIds() != null && !request.getFileIds().isEmpty()) {
            fileService.confirmFiles(
                    request.getFileIds(),
                    FileTargetType.POST,
                    savedPost.getPostId(),
                    AttachmentPurpose.POST_ATTACHMENT
            );
        }

        return PostCreateResponse.from(savedPost);
    }

    // 게시글 수정
    @Transactional
    public void updatePost(Long userId, Long postId, PostUpdateRequest request) {
        log.info("게시글 수정 - userId: {}, postId: {}", userId, postId);

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

        // 삭제된 게시글 체크
        if (post.isDeleted()) {
            throw new CustomException(PostErrorCode.DELETED_POST);
        }

        // 작성자 본인 확인
        if (!post.isAuthor(userId)) {
            throw new CustomException(PostErrorCode.UNAUTHORIZED_POST_ACCESS);
        }

        // 게시글 수정
        post.updatePost(request.getPostTitle(), request.getPostContent());

        // 파일 처리 (fileIds가 null이 아닐 때만)
        if (request.getFileIds() != null) {
            // 1. 기존 FileAttachment 조회
            List<FileAttachment> existingAttachments = fileAttachmentRepository
                    .findByFileTargetTypeAndFileTargetId(FileTargetType.POST, postId);

            // 2. 기존 파일 ID 목록
            List<Long> existingFileIds = existingAttachments.stream()
                    .map(attachment -> attachment.getFile().getFileId())
                    .toList();

            // 3. 삭제할 파일 ID (기존에는 있지만 요청에는 없는 파일)
            List<Long> fileIdsToDelete = existingFileIds.stream()
                    .filter(fileId -> !request.getFileIds().contains(fileId))
                    .toList();

            // 4. 새로 추가할 파일 ID (요청에는 있지만 기존에는 없는 TEMP 파일)
            List<Long> fileIdsToAdd = request.getFileIds().stream()
                    .filter(fileId -> !existingFileIds.contains(fileId))
                    .toList();

            // 5. 삭제할 파일은 완전 삭제 (File + S3)
            for (FileAttachment attachment : existingAttachments) {
                if (fileIdsToDelete.contains(attachment.getFile().getFileId())) {
                    fileService.detachFile(attachment.getFileAttachmentId());
                }
            }

            // 6. 새 파일만 확정 (TEMP → PERMANENT)
            if (!fileIdsToAdd.isEmpty()) {
                fileService.confirmFiles(
                        fileIdsToAdd,
                        FileTargetType.POST,
                        postId,
                        AttachmentPurpose.POST_ATTACHMENT
                );
            }

            // 유지되는 PERMANENT 파일은 Attachment 그대로 유지
        }
        // fileIds가 null이면 파일 변경 없음 (기존 파일 유지)

        log.info("게시글 수정 완료 - postId: {}", postId);
    }

    // 게시글 삭제 (Soft Delete)
    @Transactional
    public void deletePost(Long userId, Long postId) {
        log.info("게시글 삭제 - userId: {}, postId: {}", userId, postId);

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

        // 이미 삭제된 게시글 체크
        if (post.isDeleted()) {
            throw new CustomException(PostErrorCode.DELETED_POST);
        }

        // 작성자 본인 확인
        if (!post.isAuthor(userId)) {
            throw new CustomException(PostErrorCode.UNAUTHORIZED_POST_ACCESS);
        }

        // 게시판별 타입 테이블 Soft Delete
        Long boardId = post.getBoard().getBoardId();
        if (boardId == 1L) {
            // 자치회 활동 후기는 CouncilReviewPostController 사용
            throw new CustomException(PostErrorCode.INVALID_BOARD);
        } else if (boardId == 2L) {
            // 멘토링 후기
            MentoringPost mentoringPost = mentoringPostRepository.findByPostId(postId)
                    .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));
            mentoringPost.softDelete();  // 내부에서 post.softDelete() 호출됨
            log.info("멘토링 후기 삭제 - mentoringPostId: {}", mentoringPost.getMentoringPostId());
        } else if (boardId == 3L) {
            // 고민상담
            CounselingPost counselingPost = counselingPostRepository.findByPostId(postId)
                    .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));
            counselingPost.softDelete();  // 내부에서 post.softDelete() 호출됨
            log.info("고민상담 삭제 - counselingPostId: {}", counselingPost.getCounselingPostId());
        } else if (boardId == 4L) {
            // 재단소식
            NoticePost noticePost = noticePostRepository.findByPostId(postId)
                    .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));
            noticePost.softDelete();  // 내부에서 post.softDelete() 호출됨
            log.info("재단소식 삭제 - noticePostId: {}", noticePost.getNoticePostId());
        }
    }

    // 게시판별 카테고리 검증
    private void validateCategoryForBoard(Long boardId, PostCategory category) {
        // boardId=1 (자치회 활동 후기): 카테고리 불필요 (NULL)
        if (boardId == 1L) {
            if (category != null) {
                throw new CustomException(PostErrorCode.CATEGORY_NOT_ALLOWED);
            }
            return;
        }

        // boardId=2 (멘토링 후기): STUDY, ADMISSION, JOB, ETC 필수
        if (boardId == 2L) {
            if (category == null) {
                throw new CustomException(PostErrorCode.CATEGORY_REQUIRED);
            }
            if (category != PostCategory.STUDY &&
                    category != PostCategory.ADMISSION &&
                    category != PostCategory.JOB &&
                    category != PostCategory.ETC) {
                throw new CustomException(PostErrorCode.INVALID_CATEGORY_FOR_BOARD);
            }
            return;
        }

        // boardId=3 (고민상담): STUDY, ADMISSION, JOB, ETC 필수
        if (boardId == 3L) {
            if (category == null) {
                throw new CustomException(PostErrorCode.CATEGORY_REQUIRED);
            }
            if (category != PostCategory.STUDY &&
                    category != PostCategory.ADMISSION &&
                    category != PostCategory.JOB &&
                    category != PostCategory.ETC) {
                throw new CustomException(PostErrorCode.INVALID_CATEGORY_FOR_BOARD);
            }
            return;
        }

        // boardId=4 (재단소식): NOTICE, PROGRAM 필수
        if (boardId == 4L) {
            if (category == null) {
                throw new CustomException(PostErrorCode.CATEGORY_REQUIRED);
            }
            if (category != PostCategory.NOTICE && category != PostCategory.PROGRAM) {
                throw new CustomException(PostErrorCode.INVALID_CATEGORY_FOR_BOARD);
            }
            return;
        }

        // 그 외 게시판은 지원하지 않음
        throw new CustomException(PostErrorCode.INVALID_BOARD);
    }
}