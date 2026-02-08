package com.solif.backend.domain.mission.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "mission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    private Long missionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_category", nullable = false, length = 20)
    private MissionCategory missionCategory;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "mission_title", nullable = false, length = 100)
    private String missionTitle;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 50)
    private MissionConditionType conditionType;

    @Column(name = "condition_value", length = 255)
    private String conditionValue;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Mission(MissionCategory missionCategory, Integer sequenceOrder, String missionTitle,
                   String description, MissionConditionType conditionType, String conditionValue) {
        this.missionCategory = missionCategory;
        this.sequenceOrder = sequenceOrder;
        this.missionTitle = missionTitle;
        this.description = description;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
    }
}