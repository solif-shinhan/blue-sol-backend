package com.solif.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_login_id", columnNames = "login_id"),
    @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
    @UniqueConstraint(name = "uk_user_phone", columnNames = "phone"),
    @UniqueConstraint(name = "uk_user_scholar_number", columnNames = "scholar_number")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", nullable = false, length = 50)
    private String loginId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "scholar_number", length = 50)
    private String scholarNumber;

    @Column(name = "region", length = 50)
    private String region;

    @Column(name = "school_name", length = 100)
    private String schoolName;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole userRole;

    @Column(name = "job", length = 100)
    private String job;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public User(String loginId, String password, String name, String phone, 
                String email, String scholarNumber, String region, String schoolName, UserRole userRole, String job) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.scholarNumber = scholarNumber;
        this.region = region;
        this.schoolName = schoolName;
        this.userRole = userRole;
        this.job = job;
    }

    public enum UserRole {
        JUNIOR, SENIOR, GRADUATE, MASTER
    }
}
