package com.solif.backend.domain.file.repository;

import com.solif.backend.domain.file.entity.AttachmentPurpose;
import com.solif.backend.domain.file.entity.FileAttachment;
import com.solif.backend.domain.file.entity.FileTargetType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    // 특정 타입과 대상 ID로 파일 조회 (sortOrder 순서대로)
    @EntityGraph(attributePaths = {"file"})
    List<FileAttachment> findByFileTargetTypeAndFileTargetIdOrderBySortOrder(
            FileTargetType fileTargetType, Long fileTargetId);

    // 파일 전체 조회 (삭제용)
    List<FileAttachment> findByFileTargetTypeAndFileTargetId(
            FileTargetType fileTargetType, Long fileTargetId);

    // 대표 이미지 조회 (sortOrder 기반)
    @EntityGraph(attributePaths = {"file"})
    Optional<FileAttachment> findByFileTargetTypeAndFileTargetIdAndSortOrder(
            FileTargetType fileTargetType, Long fileTargetId, Integer sortOrder);

    // 대표 이미지 조회 (sortOrder + purpose 기반)
    @EntityGraph(attributePaths = {"file"})
    Optional<FileAttachment> findByFileTargetTypeAndFileTargetIdAndSortOrderAndPurpose(
            FileTargetType fileTargetType,
            Long fileTargetId,
            Integer sortOrder,
            AttachmentPurpose purpose
    );

    // 여러 대상의 대표 이미지를 한 번에 조회 (배치 쿼리)
    @EntityGraph(attributePaths = {"file"})
    List<FileAttachment> findByFileTargetTypeAndFileTargetIdInAndSortOrderAndPurpose(
            FileTargetType fileTargetType,
            List<Long> fileTargetIds,
            Integer sortOrder,
            AttachmentPurpose purpose
    );

    boolean existsByFileFileId(Long fileId);
}
