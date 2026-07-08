package com.example.movra.bc.personalization.behavior_profile.application.event;

import com.example.movra.bc.insight.behavior_insight.domain.event.ProfileDriftDetectedEvent;
import com.example.movra.bc.insight.behavior_insight.domain.event.ProfileDriftItem;
import com.example.movra.bc.personalization.behavior_profile.domain.ProfileAdjustmentSuggestion;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.ProfileAdjustmentSuggestionRepository;
import com.example.movra.bc.personalization.behavior_profile.domain.type.AdjustmentSuggestionStatus;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ProfileAdjustmentTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Clock;
import java.util.List;

/**
 * insight가 발행한 괴리 이벤트를 받아 조정 제안을 PENDING으로 기록한다.
 * AFTER_COMMIT(리포트가 실제 저장된 뒤) + REQUIRES_NEW(별도 트랜잭션)로 동작한다.
 * 같은 항목(target)에 대한 기존 PENDING 제안은 최신 분석으로 대체(DISMISSED)한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileDriftDetectedEventHandler {

    private final ProfileAdjustmentSuggestionRepository profileAdjustmentSuggestionRepository;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProfileDriftDetectedEvent event) {
        for (ProfileDriftItem item : event.items()) {
            ProfileAdjustmentTarget target = ProfileAdjustmentTarget.valueOf(item.type().name());

            List<ProfileAdjustmentSuggestion> superseded = profileAdjustmentSuggestionRepository
                    .findAllByUserIdAndTargetAndStatus(event.userId(), target, AdjustmentSuggestionStatus.PENDING);
            superseded.forEach(ProfileAdjustmentSuggestion::dismiss);
            profileAdjustmentSuggestionRepository.saveAll(superseded);

            profileAdjustmentSuggestionRepository.save(ProfileAdjustmentSuggestion.create(
                    event.userId(),
                    target,
                    item.declaredValue(),
                    item.observedValue(),
                    item.suggestedStartHour(),
                    item.suggestedEndHour(),
                    item.suggestedValue(),
                    item.message(),
                    clock.instant()
            ));
        }
        log.info("프로필 조정 제안 기록 - userId={}, 제안 수={}", event.userId().id(), event.items().size());
    }
}
