package com.solif.backend.domain.scholarshipprogrampost.service;

import com.solif.backend.domain.comment.dto.PostCommentCount;
import com.solif.backend.domain.comment.repository.CommentRepository;
import com.solif.backend.domain.file.entity.AttachmentPurpose;
import com.solif.backend.domain.file.entity.FileAttachment;
import com.solif.backend.domain.file.entity.FileTargetType;
import com.solif.backend.domain.file.repository.FileAttachmentRepository;
import com.solif.backend.domain.post.entity.PostCategory;
import com.solif.backend.domain.scholarshipprogrampost.dto.ScholarshipProgramListResponse;
import com.solif.backend.domain.scholarshipprogrampost.dto.ScholarshipProgramResponse;
import com.solif.backend.domain.scholarshipprogrampost.entity.ScholarshipProgramPost;
import com.solif.backend.domain.scholarshipprogrampost.repository.ScholarshipProgramPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScholarshipProgramService {

    private final ScholarshipProgramPostRepository scholarshipProgramPostRepository;
    private final CommentRepository commentRepository;
    private final FileAttachmentRepository fileAttachmentRepository;

    @Value("${cloud.aws.region.static}")
    private String region;

    // 장학 프로그램 목록 조회 (필수/선택 분리)
    public ScholarshipProgramListResponse getScholarshipPrograms() {
        log.info("장학 프로그램 목록 조회 시작");

        // 필수 프로그램 조회
        List<ScholarshipProgramPost> requiredPrograms =
                scholarshipProgramPostRepository.findByPostCategoryWithPostAndAuthor(PostCategory.REQUIRED);
        log.info("필수 프로그램 조회 완료 - count: {}", requiredPrograms.size());

        // 선택 프로그램 조회
        List<ScholarshipProgramPost> optionalPrograms =
                scholarshipProgramPostRepository.findByPostCategoryWithPostAndAuthor(PostCategory.OPTIONAL);
        log.info("선택 프로그램 조회 완료 - count: {}", optionalPrograms.size());

        // 댓글 수 일괄 조회 (N+1 방지)
        List<Long> allPostIds = Stream.concat(
                requiredPrograms.stream().map(spp -> spp.getPost().getPostId()),
                optionalPrograms.stream().map(spp -> spp.getPost().getPostId())
        ).collect(Collectors.toList());

        // Early return
        if (allPostIds.isEmpty()) {
            log.info("장학 프로그램 데이터 없음 - 빈 응답 반환");
            return ScholarshipProgramListResponse.builder()
                    .required(List.of())
                    .optional(List.of())
                    .build();
        }

        Map<Long, Long> commentCountMap = commentRepository.countByPostIds(allPostIds).stream()
                .collect(Collectors.toMap(
                        PostCommentCount::getPostId,
                        PostCommentCount::getCommentCount
                ));

        // 썸네일 일괄 조회 (N+1 방지) - sortOrder=1인 대표 이미지만
        List<FileAttachment> thumbnails = fileAttachmentRepository
                .findByFileTargetTypeAndTargetIdInAndSortOrderAndPurpose(
                        FileTargetType.POST,
                        allPostIds,
                        1,  // 첫 번째 이미지만
                        AttachmentPurpose.POST_ATTACHMENT
                );

        Map<Long, String> thumbnailMap = thumbnails.stream()
                .collect(Collectors.toMap(
                        FileAttachment::getTargetId,
                        attachment -> attachment.getFile().getUrl(region),
                        (existing, replacement) -> existing  // 중복 시 첫 번째 유지
                ));

        // Response 변환
        List<ScholarshipProgramResponse> requiredResponses = requiredPrograms.stream()
                .map(spp -> ScholarshipProgramResponse.from(
                        spp,
                        commentCountMap.getOrDefault(spp.getPost().getPostId(), 0L),
                        thumbnailMap.get(spp.getPost().getPostId())
                ))
                .collect(Collectors.toList());

        List<ScholarshipProgramResponse> optionalResponses = optionalPrograms.stream()
                .map(spp -> ScholarshipProgramResponse.from(
                        spp,
                        commentCountMap.getOrDefault(spp.getPost().getPostId(), 0L),
                        thumbnailMap.get(spp.getPost().getPostId())
                ))
                .collect(Collectors.toList());

        log.info("장학 프로그램 목록 조회 완료 - required: {}, optional: {}",
                requiredResponses.size(), optionalResponses.size());

        return ScholarshipProgramListResponse.builder()
                .required(requiredResponses)
                .optional(optionalResponses)
                .build();
    }
}