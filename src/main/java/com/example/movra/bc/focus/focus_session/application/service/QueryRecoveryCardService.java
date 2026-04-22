package com.example.movra.bc.focus.focus_session.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.application.service.dto.response.RecoveryCardResponse;
import com.example.movra.bc.focus.focus_session.domain.DailyFocusSummary;
import com.example.movra.bc.focus.focus_session.domain.repository.DailyFocusSummaryRepository;
import com.example.movra.bc.focus.focus_session.domain.type.RecoveryType;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.BehaviorProfileRepository;
import com.example.movra.bc.personalization.behavior_profile.domain.type.RecoveryStyle;
import com.example.movra.bc.planning.daily_plan.domain.DailyTopPicksSummary;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyTopPicksSummaryRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QueryRecoveryCardService {

    private final DailyFocusSummaryRepository dailyFocusSummaryRepository;
    private final DailyTopPicksSummaryRepository dailyTopPicksSummaryRepository;
    private final BehaviorProfileRepository behaviorProfileRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;

    @Transactional(readOnly = true)
    public RecoveryCardResponse query() {
        UserId userId = currentUserQuery.currentUser().userId();
        LocalDate yesterday = LocalDate.now(clock).minusDays(1);

        Optional<DailyFocusSummary> focusSummary = dailyFocusSummaryRepository
                .findByUserIdAndDate(userId, yesterday);
        boolean missedFocus = focusSummary.isEmpty() || focusSummary.get().getTotalSeconds() == 0;
        long yesterdayFocusSeconds = focusSummary.map(DailyFocusSummary::getTotalSeconds).orElse(0L);

        Optional<DailyTopPicksSummary> topPicksSummary = dailyTopPicksSummaryRepository
                .findByUserIdAndDate(userId, yesterday);
        boolean incompleteTopPick = topPicksSummary
                .map(summary -> summary.getTotalCount() > 0 && summary.getCompletedCount() < summary.getTotalCount())
                .orElse(false);
        double yesterdayTopPickCompletionRate = topPicksSummary
                .filter(summary -> summary.getTotalCount() > 0)
                .map(summary -> (double) summary.getCompletedCount() / summary.getTotalCount())
                .orElse(0.0);

        RecoveryType recoveryType = determineRecoveryType(missedFocus, incompleteTopPick);

        String suggestedAction = determineSuggestedAction(userId, recoveryType);

        return RecoveryCardResponse.builder()
                .needsRecovery(recoveryType != RecoveryType.NONE)
                .recoveryType(recoveryType)
                .suggestedAction(suggestedAction)
                .yesterdayFocusSeconds(yesterdayFocusSeconds)
                .yesterdayTopPickCompletionRate(yesterdayTopPickCompletionRate)
                .build();
    }

    private RecoveryType determineRecoveryType(boolean missedFocus, boolean incompleteTopPick) {
        if (missedFocus && incompleteTopPick) {
            return RecoveryType.BOTH;
        }
        if (missedFocus) {
            return RecoveryType.MISSED_FOCUS;
        }
        if (incompleteTopPick) {
            return RecoveryType.INCOMPLETE_TOP_PICK;
        }
        return RecoveryType.NONE;
    }

    private String determineSuggestedAction(UserId userId, RecoveryType recoveryType) {
        if (recoveryType == RecoveryType.NONE) {
            return null;
        }

        return behaviorProfileRepository.findByUserId(userId)
                .map(BehaviorProfile::getRecoveryStyle)
                .map(this::messageForRecoveryStyle)
                .orElse("다시 시작해볼까요?");
    }

    private String messageForRecoveryStyle(RecoveryStyle recoveryStyle) {
        return switch (recoveryStyle) {
            case QUICK_RESTART -> "어제는 쉬어갔어요. 지금 바로 시작해볼까요?";
            case NEEDS_REFLECTION -> "어제 무엇이 어려웠는지 한 줄만 남겨볼까요?";
            case SLOW_REBUILDER -> "5분만 해볼까요? 작게 시작하면 돼요.";
        };
    }
}
