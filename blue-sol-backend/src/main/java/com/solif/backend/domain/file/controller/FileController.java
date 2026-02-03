package com.solif.backend.domain.file.controller;

import com.solif.backend.domain.file.dto.request.FileAttachRequest;
import com.solif.backend.domain.file.dto.response.FileAttachmentResponse;
import com.solif.backend.domain.file.dto.response.FileUploadResponse;
import com.solif.backend.domain.file.entity.TargetType;
import com.solif.backend.domain.file.service.FileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "파일", description = "파일 API")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 단일/다중 파일 업로드
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<FileUploadResponse>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "folder", defaultValue = "uploads") String folder) {

        List<FileUploadResponse> responses = fileService.uploadFiles(files, folder);
        return ResponseEntity.ok(responses);
    }

    /**
     * 파일-엔티티 연결
     */
    @PostMapping("/attachments")
    public ResponseEntity<FileAttachmentResponse> attachFile(
            @Valid @RequestBody FileAttachRequest request) {

        FileAttachmentResponse response = fileService.attachFile(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 파일 연결 해제
     */
    @DeleteMapping("/attachments/{fileAttachmentId}")
    public ResponseEntity<Void> detachFile(
            @PathVariable Long fileAttachmentId) {

        fileService.detachFile(fileAttachmentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 엔티티의 첨부파일 조회
     */
    @GetMapping("/attachments")
    public ResponseEntity<List<FileAttachmentResponse>> getAttachments(
            @RequestParam TargetType targetType,
            @RequestParam Long targetId) {

        List<FileAttachmentResponse> responses = fileService.getAttachments(targetType, targetId);
        return ResponseEntity.ok(responses);
    }
}
