package com.solif.backend.domain.scholarshipprogrampost.controller;

import com.solif.backend.domain.scholarshipprogrampost.code.ScholarshipProgramSuccessCode;
import com.solif.backend.domain.scholarshipprogrampost.dto.ScholarshipProgramListResponse;
import com.solif.backend.domain.scholarshipprogrampost.service.ScholarshipProgramService;
import com.solif.backend.global.common.response.ResponseFactory;
import com.solif.backend.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "장학 프로그램", description = "장학 프로그램 API")
@RestController
@RequestMapping("/api/v1/scholarship-programs")
@RequiredArgsConstructor
public class ScholarshipProgramController {

    private final ScholarshipProgramService scholarshipProgramService;

    @Operation(summary = "장학 프로그램 목록 조회", description = "필수/선택 프로그램을 분리하여 조회합니다.")
    @GetMapping
    public ResponseEntity<SuccessResponse<ScholarshipProgramListResponse>> getScholarshipPrograms() {
        ScholarshipProgramListResponse response = scholarshipProgramService.getScholarshipPrograms();
        return ResponseFactory.success(ScholarshipProgramSuccessCode.SCHOLARSHIP_PROGRAM_LIST_SUCCESS, response);
    }
}