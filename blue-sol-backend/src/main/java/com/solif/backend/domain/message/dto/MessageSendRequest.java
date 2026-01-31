package com.solif.backend.domain.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "쪽지 발송 요청")
public class MessageSendRequest {

    @NotNull(message = "받는 사람 ID는 필수입니다.")
    @Schema(description = "받는 사람 ID", example = "2")
    private Long receiverId;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
    @Schema(description = "쪽지 제목", example = "자산 관리의 어려움")
    private String messageTitle;

    @NotBlank(message = "내용은 필수입니다.")
    @Schema(description = "쪽지 내용", example = "대학생이 된 후 저의 자산을 어떻게 관리하는지 어려움을 느끼고 있습니다.")
    private String messageContent;

    @Schema(description = "첨부 파일 ID 목록", nullable = true)
    private List<Long> fileIds;
}
