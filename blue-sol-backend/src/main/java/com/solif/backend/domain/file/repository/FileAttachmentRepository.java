package com.solif.backend.domain.file.repository;

import com.solif.backend.domain.file.entity.FileAttachment;
import com.solif.backend.domain.file.entity.FileTargetType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    @EntityGraph(attributePaths = {"file"})
    List<FileAttachment> findByFileTargetTypeAndTargetIdOrderBySortOrder(
            FileTargetType fileTargetType, Long targetId);

    boolean existsByFileFileId(Long fileId);
}
