package com.solif.backend.domain.network.repository;

import com.solif.backend.domain.network.entity.Connection;
import com.solif.backend.domain.network.entity.ConnectionStatus;
import com.solif.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    // 나의 교류망 목록 조회 (register 기준)
    List<Connection> findAllByRegisterAndStatus(User register, ConnectionStatus status);

    // 이미 연결되어 있는지 확인
    boolean existsByRegisterAndTarget(User register, User target);

    // 교류망 수 카운트
    long countByRegisterAndStatus(User register, ConnectionStatus status);

    // 특정 사용자와의 연결 여부 확인 (양방향)
    boolean existsByRegisterAndTargetOrTargetAndRegister(User register1, User target1, User target2, User register2);
}
