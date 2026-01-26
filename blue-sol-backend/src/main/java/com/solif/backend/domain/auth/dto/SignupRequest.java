package com.solif.backend.domain.auth.dto;

import com.solif.backend.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "회원가입 요청")
public class SignupRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "아이디는 영문, 숫자, 언더스코어만 사용 가능합니다.")
    @Schema(description = "로그인 아이디", example = "user1234")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$", 
             message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    @Schema(description = "비밀번호", example = "password123!")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    @Schema(description = "이름", example = "홍길동")
    private String name;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^01[0-9]-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @NotBlank(message = "학번은 필수입니다.")
    @Schema(description = "학번", example = "20240001")
    private String scholarNumber;

    @NotNull(message = "사용자 역할은 필수입니다.")
    @Schema(description = "사용자 역할", example = "JUNIOR", allowableValues = {"JUNIOR", "SENIOR", "GRADUATE", "MASTER"})
    private User.UserRole userRole;

    @Schema(description = "지역 (선택사항)", example = "서울")
    private String region;

    @Schema(description = "학교명 (선택사항)", example = "서울대학교")
    private String schoolName;
}
