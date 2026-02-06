package com.solif.backend.domain.goal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solif.backend.domain.goal.dto.GoalCountResponse;
import com.solif.backend.domain.goal.dto.GoalFirstResponse;
import com.solif.backend.domain.goal.code.GoalErrorCode;
import com.solif.backend.domain.profile.entity.UserProfile;
import com.solif.backend.domain.profile.repository.UserProfileRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalService {

    private final UserProfileRepository userProfileRepository;
    private final ObjectMapper objectMapper;

    //첫 번째 목표 조회
    public GoalFirstResponse getFirstGoal(Long userId) {
        UserProfile profile = userProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new CustomException(GoalErrorCode.PROFILE_NOT_FOUND));

        List<String> goals = convertJsonToList(profile.getMainGoal());

        if (goals.isEmpty()) {
            throw new CustomException(GoalErrorCode.GOAL_NOT_FOUND);
        }

        return GoalFirstResponse.of(goals.get(0), 1, goals.size());
    }

    //목표 개수 조회
    public GoalCountResponse getGoalCount(Long userId) {
        UserProfile profile = userProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new CustomException(GoalErrorCode.PROFILE_NOT_FOUND));

        List<String> goals = convertJsonToList(profile.getMainGoal());

        return GoalCountResponse.of(goals.size());
    }

    private List<String> convertJsonToList(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }
}
