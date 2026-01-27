package com.solif.backend.domain.user.dto;

import com.solif.backend.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserMeResponse {
    private Long userId;
    private String loginId;
    private String name;
    private String phone;
    private String email;
    private String scholarNumber;
    private String region;
    private String schoolName;
    private String userRole;
    private LocalDateTime createdAt;

    public static UserMeResponse from(User user) {
        return UserMeResponse.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .scholarNumber(user.getScholarNumber())
                .region(user.getRegion())
                .schoolName(user.getSchoolName())
                .userRole(user.getUserRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
