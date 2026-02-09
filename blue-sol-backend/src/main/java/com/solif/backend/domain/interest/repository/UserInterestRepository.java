package com.solif.backend.domain.interest.repository;

import com.solif.backend.domain.interest.entity.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    
    // 사용자의 기존 관심사 삭제 (덮어쓰기용)
    void deleteAllByUser_UserId(Long userId);
    
    // 사용자의 관심사 조회
    List<UserInterest> findAllByUser_UserId(Long userId);

    // 특정 관심사를 가진 사용자 조회
    List<UserInterest> findAllByCategoryNameIn(List<String> categoryNames);

    // 여러 사용자 ID로 관심사 배치 조회
    List<UserInterest> findAllByUser_UserIdIn(List<Long> userIds);
}
