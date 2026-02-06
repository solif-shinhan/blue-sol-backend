package com.solif.backend.domain.file.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_attachment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class FileAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_attachment_id")
    private Long fileAttachmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 50)
    private FileTargetType fileTargetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 50)
    private AttachmentPurpose purpose;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 1;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public FileAttachment(File file, FileTargetType fileTargetType, Long targetId,
                          AttachmentPurpose purpose, Integer sortOrder) {
        this.file = file;
        this.fileTargetType = fileTargetType;
        this.targetId = targetId;
        this.purpose = purpose;
        this.sortOrder = sortOrder != null ? sortOrder : 1;
    }
}
