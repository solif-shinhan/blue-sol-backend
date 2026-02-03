package com.solif.backend.domain.file.repository;

import com.solif.backend.domain.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
