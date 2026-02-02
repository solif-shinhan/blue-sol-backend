package com.solif.backend.domain.user.repository;

import com.solif.backend.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByLoginId(String loginId);
    
    boolean existsByLoginId(String loginId);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
    
    boolean existsByScholarNumber(String scholarNumber);

    // 이름으로 검색
    List<User> findByNameContaining(String name);

    // 이름으로 검색 (페이징, 정렬)
    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword% ORDER BY u.name")
    List<User> searchByName(@Param("keyword") String keyword, Pageable pageable);
}
