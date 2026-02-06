package com.solif.backend.domain.file.repository;

import com.solif.backend.domain.file.entity.File;
import com.solif.backend.domain.file.entity.FileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {

    @Query("SELECT f FROM File f WHERE f.status = :status AND f.createdAt < :expiredTime")
    List<File> findExpiredTempFiles(
            @Param("status") FileStatus status,
            @Param("expiredTime") LocalDateTime expiredTime
    );

    List<File> findAllByFileIdIn(List<Long> fileIds);
}
