package com.solif.backend.domain.scholarshipprogrampost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "장학 프로그램 목록 응답")
public class ScholarshipProgramListResponse {

    @Schema(description = "필수 프로그램 목록")
    private List<ScholarshipProgramResponse> required;

    @Schema(description = "선택 프로그램 목록")
    private List<ScholarshipProgramResponse> optional;
}