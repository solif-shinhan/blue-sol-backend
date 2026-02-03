package com.solif.backend.domain.file.service;

import com.solif.backend.domain.file.dto.request.FileAttachRequest;
import com.solif.backend.domain.file.dto.response.FileAttachmentResponse;
import com.solif.backend.domain.file.dto.response.FileUploadResponse;
import com.solif.backend.domain.file.entity.File;
import com.solif.backend.domain.file.entity.FileAttachment;
import com.solif.backend.domain.file.entity.TargetType;
import com.solif.backend.domain.file.exception.FileException;
import com.solif.backend.domain.file.repository.FileAttachmentRepository;
import com.solif.backend.domain.file.repository.FileRepository;
import com.solif.backend.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.solif.backend.domain.file.exception.FileException.FileErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private final FileRepository fileRepository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final S3Service s3Service;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    /**
     * 단일/다중 파일 업로드
     */
    @Transactional
    public List<FileUploadResponse> uploadFiles(List<MultipartFile> files, String folder) {
        List<FileUploadResponse> responses = new ArrayList<>();

        for (MultipartFile multipartFile : files) {
            try {
                String originalName = multipartFile.getOriginalFilename();
                String extension = extractExtension(originalName);
                String fileName = UUID.randomUUID() + extension;
                String contentType = multipartFile.getContentType();
                long sizeBytes = multipartFile.getSize();

                // S3 업로드
                s3Service.upload(folder, fileName, multipartFile.getBytes(), contentType);

                // DB 저장
                File file = File.builder()
                        .bucket(bucket)
                        .objectKey(folder + "/" + fileName)
                        .originalName(originalName)
                        .contentType(contentType)
                        .sizeBytes(sizeBytes)
                        .build();

                fileRepository.save(file);
                responses.add(FileUploadResponse.from(file, region));

            } catch (IOException e) {
                log.error("파일 업로드 실패: {}", e.getMessage());
                throw new FileException(FILE_UPLOAD_FAILED);
            }
        }

        return responses;
    }

    /**
     * 파일-엔티티 연결
     */
    @Transactional
    public FileAttachmentResponse attachFile(FileAttachRequest request) {
        File file = fileRepository.findById(request.getFileId())
                .orElseThrow(() -> new FileException(FILE_NOT_FOUND));

        FileAttachment attachment = FileAttachment.builder()
                .file(file)
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .purpose(request.getPurpose())
                .sortOrder(request.getSortOrder())
                .build();

        fileAttachmentRepository.save(attachment);

        return FileAttachmentResponse.from(attachment, region);
    }

    /**
     * 파일 연결 해제
     */
    @Transactional
    public void detachFile(Long fileAttachmentId) {
        FileAttachment attachment = fileAttachmentRepository.findById(fileAttachmentId)
                .orElseThrow(() -> new FileException(FILE_ATTACHMENT_NOT_FOUND));

        fileAttachmentRepository.delete(attachment);
    }

    /**
     * 특정 엔티티의 첨부파일 조회
     */
    public List<FileAttachmentResponse> getAttachments(TargetType targetType, Long targetId) {
        List<FileAttachment> attachments = fileAttachmentRepository
                .findByTargetTypeAndTargetIdOrderBySortOrder(targetType, targetId);

        return attachments.stream()
                .map(attachment -> FileAttachmentResponse.from(attachment, region))
                .toList();
    }

    private String extractExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            return "";
        }
        return originalName.substring(originalName.lastIndexOf("."));
    }
}
