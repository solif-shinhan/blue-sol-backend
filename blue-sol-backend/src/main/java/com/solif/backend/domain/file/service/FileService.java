package com.solif.backend.domain.file.service;

import com.solif.backend.domain.file.dto.request.FileAttachRequest;
import com.solif.backend.domain.file.dto.response.FileAttachmentResponse;
import com.solif.backend.domain.file.dto.response.FileUploadResponse;
import com.solif.backend.domain.file.entity.*;
import com.solif.backend.domain.file.exception.FileException;
import com.solif.backend.domain.file.repository.FileAttachmentRepository;
import com.solif.backend.domain.file.repository.FileRepository;
import com.solif.backend.global.common.exception.CustomException;
import com.solif.backend.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
     * 파일 업로드
     * - S3 업로드 후 status=TEMP로 저장
     * - 글쓰기 완료 시 confirmFiles()로 PERMANENT 변경
     */
    @Transactional
    public List<FileUploadResponse> uploadFiles(List<MultipartFile> files, FileFolder folder) {
        List<FileUploadResponse> responses = new ArrayList<>();
        List<String> uploadedKeys = new ArrayList<>();

        String folderName = folder.getFolderName();

        try {
            for (MultipartFile multipartFile : files) {
                String originalName = multipartFile.getOriginalFilename();
                String fileName = UUID.randomUUID() + extractExtension(originalName);
                String contentType = multipartFile.getContentType();
                String objectKey = folderName + "/" + fileName;

                s3Service.upload(folderName, fileName, multipartFile.getBytes(), contentType);
                uploadedKeys.add(objectKey);

                log.info("파일 S3 업로드 - Folder: {}, OriginalName: {}, Key: {}", folderName, originalName, objectKey);

                File file = File.builder()
                        .bucket(bucket)
                        .objectKey(objectKey)
                        .originalName(originalName)
                        .contentType(contentType)
                        .sizeBytes(multipartFile.getSize())
                        .status(FileStatus.TEMP)
                        .build();

                fileRepository.save(file);
                responses.add(FileUploadResponse.from(file, region));
            }

            log.info("총 {}개의 파일 업로드 완료 - Folder: {}, status=TEMP", responses.size(), folderName);
            return responses;

        } catch (Exception e) {
            log.error("파일 업로드 실패. 롤백 중... 삭제할 파일: {}개", uploadedKeys.size());
            for (String key : uploadedKeys) {
                try {
                    s3Service.delete(key);
                } catch (Exception ignore) {
                    log.warn("Cleanup 실패 - Key: {}", key);
                }
            }

            if (e instanceof FileException) throw (FileException) e;
            if (e instanceof CustomException) throw (CustomException) e;
            throw new FileException(FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 파일 확정 (글쓰기 API 내부에서 호출)
     * - TEMP → PERMANENT 상태 변경
     * - FileAttachment 생성
     */
    @Transactional
    public List<FileAttachmentResponse> confirmFiles(List<Long> fileIds, TargetType targetType,
                                                      Long targetId, AttachmentPurpose purpose) {
        if (fileIds == null || fileIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<File> files = fileRepository.findAllByFileIdIn(fileIds);

        if (files.size() != fileIds.size()) {
            throw new FileException(FILE_NOT_FOUND);
        }

        List<FileAttachmentResponse> responses = new ArrayList<>();
        int sortOrder = 1;

        for (File file : files) {
            file.confirm();

            FileAttachment attachment = FileAttachment.builder()
                    .file(file)
                    .targetType(targetType)
                    .targetId(targetId)
                    .purpose(purpose)
                    .sortOrder(sortOrder++)
                    .build();

            fileAttachmentRepository.save(attachment);
            responses.add(FileAttachmentResponse.from(attachment, region));
        }

        log.info("{}개의 파일 확정 완료 - targetType: {}, targetId: {}",
                files.size(), targetType, targetId);
        return responses;
    }

    /**
     * 만료된 임시 파일 삭제 (스케줄러용)
     * - 모든 폴더의 TEMP 파일을 일괄 삭제
     */
    @Transactional
    public int cleanupExpiredTempFiles(int hoursToExpire) {
        LocalDateTime expiredTime = LocalDateTime.now().minusHours(hoursToExpire);
        List<File> expiredFiles = fileRepository.findExpiredTempFiles(FileStatus.TEMP, expiredTime);

        int deletedCount = 0;
        for (File file : expiredFiles) {
            try {
                s3Service.delete(file.getObjectKey());
                fileRepository.delete(file);
                deletedCount++;
                log.info("만료된 임시 파일 삭제 - fileId: {}, key: {}",
                        file.getFileId(), file.getObjectKey());
            } catch (Exception e) {
                log.error("임시 파일 삭제 실패 - fileId: {}, error: {}",
                        file.getFileId(), e.getMessage());
            }
        }

        log.info("임시 파일 정리 완료 - 삭제: {}개 / 대상: {}개", deletedCount, expiredFiles.size());
        return deletedCount;
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

        File file = attachment.getFile();
        String objectKey = file.getObjectKey();

        fileAttachmentRepository.delete(attachment);
        fileRepository.delete(file);

        s3Service.delete(objectKey);
        log.info("파일 삭제 완료 - ID: {}, S3 Key: {}", file.getFileId(), objectKey);
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
