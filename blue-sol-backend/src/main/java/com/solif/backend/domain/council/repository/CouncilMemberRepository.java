package com.solif.backend.domain.council.repository;

import com.solif.backend.domain.council.entity.Council;
import com.solif.backend.domain.council.entity.CouncilMember;
import com.solif.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouncilMemberRepository extends JpaRepository<CouncilMember, Long> {

    // 자치회와 사용자로 멤버 조회
    Optional<CouncilMember> findByCouncilAndUser(Council council, User user);

    // 자치회로 멤버 수 조회
    Long countByCouncil(Council council);

    // 자치회로 멤버 목록 조회 (User 정보 JOIN FETCH)
    @Query("SELECT cm FROM CouncilMember cm JOIN FETCH cm.user WHERE cm.council = :council ORDER BY cm.role DESC, cm.joinedAt ASC")
    List<CouncilMember> findByCouncilOrderByRoleDescJoinedAtAsc(@Param("council") Council council);

    // 사용자로 소속 자치회 조회
    Optional<CouncilMember> findByUser(User user);

    // 자치회와 사용자 목록으로 멤버 삭제
    @Modifying
    @Query("DELETE FROM CouncilMember cm WHERE cm.council = :council AND cm.user IN :users")
    int deleteByCouncilAndUserIn(@Param("council") Council council, @Param("users") List<User> users);
}