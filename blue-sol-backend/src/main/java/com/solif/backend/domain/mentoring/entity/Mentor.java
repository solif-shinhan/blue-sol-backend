package com.solif.backend.domain.mentoring.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "mentor")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Mentor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mentor_id")
    private Long mentorId;

    @Column(name = "mentor_title", nullable = false, length = 150)
    private String mentorTitle;

    @Column(name = "mentor_name", nullable = false, length = 50)
    private String mentorName;

    @Column(name = "mentor_intro", nullable = false, columnDefinition = "TEXT")
    private String mentorIntro;

    @JoinColumn(name = "profile_image_file")
    private String profileImageFile;

    @Enumerated(EnumType.STRING)
    @Column(name = "mentor_category", nullable = false, length = 50)
    private MentorCategory mentorCategory;


    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Mentor(String mentorTitle, String mentorName, String mentorIntro,
                  String profileImageFile, MentorCategory mentorCategory, Boolean isActive) {
        this.mentorTitle = mentorTitle;
        this.mentorName = mentorName;
        this.mentorIntro = mentorIntro;
        this.profileImageFile = profileImageFile;
        this.mentorCategory = mentorCategory;
        this.isActive = isActive != null ? isActive : true;
    }
}