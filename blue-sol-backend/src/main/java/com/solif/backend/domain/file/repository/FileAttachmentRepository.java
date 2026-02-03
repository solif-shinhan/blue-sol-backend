package com.solif.backend.domain.file.repository;

import com.solif.backend.domain.file.entity.FileAttachment;
import com.solif.backend.domain.file.entity.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    List<FileAttachment> findByTargetTypeAndTargetIdOrderBySortOrder(
            TargetType targetType, Long targetId);

    boolean existsByFileFileId(Long fileId);
}
