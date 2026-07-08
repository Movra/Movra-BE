package com.example.movra.bc.insight.behavior_insight.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.BehaviorProfileView;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.BehaviorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BehaviorProfileReadAdapter implements BehaviorProfileReadPort {

    private final BehaviorProfileRepository behaviorProfileRepository;

    @Override
    public Optional<BehaviorProfileView> findProfile(UserId userId) {
        return behaviorProfileRepository.findByUserId(userId)
                .map(profile -> new BehaviorProfileView(
                        profile.getPreferredFocusStartHour(),
                        profile.getPreferredFocusEndHour(),
                        profile.getCoachingMode().name(),
                        profile.getRecoveryStyle().name(),
                        profile.getExecutionDifficulty().name()
                ));
    }
}
