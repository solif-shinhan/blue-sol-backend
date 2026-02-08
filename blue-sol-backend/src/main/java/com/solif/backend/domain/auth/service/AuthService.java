package com.solif.backend.domain.auth.service;

import com.solif.backend.domain.auth.dto.LoginRequest;
import com.solif.backend.domain.auth.dto.LoginResponse;
import com.solif.backend.domain.auth.dto.SignupRequest;
import com.solif.backend.domain.auth.dto.SignupResponse;
import com.solif.backend.domain.auth.code.AuthErrorCode;
import com.solif.backend.domain.mission.service.MissionService;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import com.solif.backend.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MissionService missionService;

    //회원가입
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        log.info("회원가입 시도 - loginId: {}", request.getLoginId());

        // 역할별 유효성 검증
        request.validateByRole();

        // 중복 검증
        validateDuplicateUser(request);

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 사용자 생성
        User user = User.builder()
                .loginId(request.getLoginId())
                .password(encodedPassword)
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .scholarNumber(request.getScholarNumber())
                .region(request.getRegion())
                .schoolName(request.getSchoolName())
                .job(request.getJob())
                .userRole(request.getUserRole())
                .build();

        User savedUser = userRepository.save(user);

        // 미션 초기화 (9개 미션 생성)
        try {
            missionService.initializeUserMissions(savedUser);
        } catch (Exception e) {
            log.warn("미션 초기화 실패 - userId: {}, error: {}", savedUser.getUserId(), e.getMessage());
        }

        log.info("회원가입 성공 - userId: {}", savedUser.getUserId());

        return SignupResponse.of(savedUser.getUserId(), savedUser.getUserRole(), savedUser.getCreatedAt());
    }

    //로그인
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("로그인 시도 - loginId: {}", request.getLoginId());

        // 사용자 조회
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_CREDENTIALS));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("로그인 실패 - 비밀번호 불일치: {}", request.getLoginId());
            throw new CustomException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        log.info("로그인 성공 - userId: {}", user.getUserId());

        // JWT 토큰 생성 (accessToken만)
        String token = jwtUtil.generateAccessToken(user.getUserId(), user.getLoginId());

        return LoginResponse.of(token, user.getUserId(), user.getUserRole());
    }

    //중복 검증
    private void validateDuplicateUser(SignupRequest request) {
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_LOGIN_ID);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_PHONE);
        }
        if (request.getUserRole() != User.UserRole.GRADUATE
                && request.getScholarNumber() != null
                && userRepository.existsByScholarNumber(request.getScholarNumber())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_SCHOLAR_NUMBER);
        }
    }
}
