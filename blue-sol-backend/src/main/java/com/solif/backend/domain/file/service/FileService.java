package com.solif.backend.domain.file.service;

import com.solif.backend.domain.file.dto.request.FileAttachRequest;
import com.solif.backend.domain.file.dto.response.FileAttachmentResponse;
import com.solif.backend.domain.file.dto.response.FileUploadResponse;
import com.solif.backend.domain.file.entity.File;
import com.solif.backend.domain.file.entity.FileAttachment;
import com.solif.backend.domain.file.entity.TargetType;
import com.solif.backend.domain.file.code.FileException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.solif.backend.domain.file.code.FileException.FileErrorCode.*;

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
        List<String> uploadedKeys = new ArrayList<>(); // 2. 원자성 확보를 위한 업로드 목록 추적

        try {
            for (MultipartFile multipartFile : files) {
                String originalName = multipartFile.getOriginalFilename();
                String fileName = UUID.randomUUID() + extractExtension(originalName);
                String contentType = multipartFile.getContentType();
                String objectKey = folder + "/" + fileName;

                // 1. S3Service에서 이미 CustomException을 던지므로 중복 래핑 없이 호출
                s3Service.upload(folder, fileName, multipartFile.getBytes(), contentType);
                uploadedKeys.add(objectKey); // 업로드 성공 시 키 기록

                // 3. 로그 보강: 업로드된 파일 정보 기록
                log.info("S3 업로드 진행 중 - OriginalName: {}, Key: {}", originalName, objectKey);

                File file = File.builder()
                        .bucket(bucket)
                        .objectKey(objectKey)
                        .originalName(originalName)
                        .contentType(contentType)
                        .sizeBytes(multipartFile.getSize())
                        .build();

                fileRepository.save(file);
                responses.add(FileUploadResponse.from(file, region));
            }

            log.info("총 {}개의 파일 업로드 및 DB 저장 완료", responses.size());
            return responses;

        } catch (Exception e) {
            // 2. 원자성 확보: 실패 시 이미 S3에 올라간 파일들 삭제 (Cleanup)
            log.error("파일 업로드 과정 중 에러 발생. 이미 업로드된 파일 {}개를 삭제합니다.", uploadedKeys.size());
            for (String key : uploadedKeys) {
                try {
                    s3Service.delete(key);
                } catch (Exception ignore) {
                    log.warn("Cleanup 중 파일 삭제 실패 - Key: {}", key);
                }
            }

            // 1. 예외 일관성 해결: 명시적 형변환으로 에러 해결
            if (e instanceof FileException) {
                throw (FileException) e;
            }
            if (e instanceof CustomException) {
                throw (CustomException) e;
            }

            throw new FileException(FILE_UPLOAD_FAILED);
        }
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
        // 1. 첨부 정보 조회
        FileAttachment attachment = fileAttachmentRepository.findById(fileAttachmentId)
                .orElseThrow(() -> new FileException(FILE_ATTACHMENT_NOT_FOUND));

        File file = attachment.getFile();
        String objectKey = file.getObjectKey(); // S3 삭제에 필요한 Key

        // 2. DB 레코드 삭제 (연결 정보 및 파일 정보)
        fileAttachmentRepository.delete(attachment);
        fileRepository.delete(file);

        // 3. S3 실제 파일 삭제 (DB 삭제 성공 후 수행)
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
