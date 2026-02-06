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
@Table(name = "pinecone_memory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PineconeMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pinecone_memory_id")
    private Long pineconeMemoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_pinecone_id", nullable = false)
    private UserPinecone userPinecone;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 50)
    private PineconeSourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public PineconeMemory(UserPinecone userPinecone, PineconeSourceType sourceType, Long sourceId) {
        this.userPinecone = userPinecone;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
    }
}