package com.solif.backend.domain.file.repository;

import com.solif.backend.domain.file.entity.FileAttachment;
import com.solif.backend.domain.file.entity.FileTargetType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    @EntityGraph(attributePaths = {"file"})
    List<FileAttachment> findByFileTargetTypeAndTargetIdOrderBySortOrder(
            FileTargetType fileTargetType, Long targetId);

    // 파일 전체 조회 (삭제용)
    List<FileAttachment> findByFileTargetTypeAndTargetId(
            FileTargetType fileTargetType, Long targetId);

    // 대표 이미지 조회 (sortOrder=1)
    @EntityGraph(attributePaths = {"file"})
    Optional<FileAttachment> findByFileTargetTypeAndTargetIdAndSortOrder(
            FileTargetType fileTargetType, Long targetId, Integer sortOrder);

    boolean existsByFileFileId(Long fileId);
}
