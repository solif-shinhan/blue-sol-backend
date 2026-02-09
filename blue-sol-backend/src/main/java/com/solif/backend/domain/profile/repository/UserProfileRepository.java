package com.solif.backend.domain.profile.repository;

import com.solif.backend.domain.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUser_UserId(Long userId);

    boolean existsByUser_UserId(Long userId);

    // QR 코드로 프로필 조회
    Optional<UserProfile> findByQrCodeData(String qrCodeData);

    // 여러 사용자 ID로 프로필 배치 조회
    List<UserProfile> findByUser_UserIdIn(List<Long> userIds);
}
