package com.solif.backend.domain.file.controller;

import com.solif.backend.domain.file.dto.request.FileAttachRequest;
import com.solif.backend.domain.file.dto.response.FileAttachmentResponse;
import com.solif.backend.domain.file.dto.response.FileUploadResponse;
import com.solif.backend.domain.file.entity.TargetType;
import com.solif.backend.domain.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    @Operation(summary = "파일 업로드",
            description = "파일을 S3에 업로드합니다. " + "폴더 타입: COUNCIL_REVIEW(자치회), POST(게시판), MENTORING(멘토링)",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<FileUploadResponse>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "folder", defaultValue = "uploads") String folder) {

        List<FileUploadResponse> responses = fileService.uploadFiles(files, folder);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "파일 연결",
            description = "업로드된 파일을 특정 엔티티와 연결합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping("/attachments")
    public ResponseEntity<FileAttachmentResponse> attachFile(
            @Valid @RequestBody FileAttachRequest request) {

        FileAttachmentResponse response = fileService.attachFile(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 파일 연결 해제
     */
    @Operation(summary = "파일 연결 해제",
            description = "파일 연결을 해제하고 S3에서 삭제합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @DeleteMapping("/attachments/{fileAttachmentId}")
    public ResponseEntity<Void> detachFile(
            @PathVariable Long fileAttachmentId) {

        fileService.detachFile(fileAttachmentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 엔티티의 첨부파일 조회
     */
    @Operation(summary = "첨부파일 조회",
            description = "특정 엔티티에 연결된 첨부파일 목록을 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @GetMapping("/attachments")
    public ResponseEntity<List<FileAttachmentResponse>> getAttachments(
            @RequestParam TargetType targetType,
            @RequestParam Long targetId) {

        List<FileAttachmentResponse> responses = fileService.getAttachments(targetType, targetId);
        return ResponseEntity.ok(responses);
    }
}
