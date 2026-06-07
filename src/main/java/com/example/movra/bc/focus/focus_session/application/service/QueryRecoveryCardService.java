package com.example.movra.bc.focus.focus_session.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.focus.focus_session.application.service.dto.response.RecoveryCardResponse;
import com.example.movra.bc.focus.focus_session.application.service.support.RecoveryGuidanceResolver;
import com.example.movra.bc.focus.focus_session.domain.DailyFocusSummary;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.RecoveryTypePolicy;
import com.example.movra.bc.focus.focus_session.domain.repository.DailyFocusSummaryRepository;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import com.example.movra.bc.focus.focus_session.domain.type.RecoveryType;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.BehaviorProfileRepository;
import com.example.movra.bc.planning.daily_plan.domain.DailyTopPicksSummary;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyTopPicksSummaryRepository;
import com.example.movra.bc.planning.exam_schedule.domain.ExamSchedule;
import com.example.movra.bc.planning.exam_schedule.domain.repository.ExamScheduleRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QueryRecoveryCardService {

    private final DailyFocusSummaryRepository dailyFocusSummaryRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final DailyTopPicksSummaryRepository dailyTopPicksSummaryRepository;
    private final BehaviorProfileRepository behaviorProfileRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;
    private final AnalyticsEventRecorder analyticsEventRecorder;

    @Transactional(readOnly = true)
    public RecoveryCardResponse query() {
        UserId userId = currentUserQuery.currentUser().userId();
        LocalDate today = LocalDate.now(clock);
        LocalDate yesterday = today.minusDays(1);

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

        Optional<ExamSchedule> recentPostExam = findRecentPostExam(userId, today);
        Optional<Long> daysSinceLastSession = daysSinceLastCompletedSession(userId, today);
        RecoveryType recoveryType = RecoveryTypePolicy.determine(
                missedFocus, incompleteTopPick, recentPostExam.isPresent(), daysSinceLastSession.orElse(null));
        Optional<BehaviorProfile> behaviorProfile = behaviorProfileRepository.findByUserId(userId);

        RecoveryCardResponse response = RecoveryCardResponse.builder()
                .needsRecovery(recoveryType != RecoveryType.NONE)
                .recoveryType(recoveryType)
                .suggestedAction(RecoveryGuidanceResolver.resolveSuggestedAction(behaviorProfile, recoveryType))
                .suggestedDurationMinutes(RecoveryGuidanceResolver.resolveSuggestedDurationMinutes(behaviorProfile, recoveryType))
                .yesterdayFocusSeconds(yesterdayFocusSeconds)
                .yesterdayTopPickCompletionRate(yesterdayTopPickCompletionRate)
                .daysSinceLastSession(daysSinceLastSession.orElse(null))
                .build();
        response = withPostExamMode(response, recentPostExam, today);

        analyticsEventRecorder.recordSafely(
                userId,
                AnalyticsEventType.RECOVERY_CARD_VIEWED,
                analyticsProperties(response, yesterday)
        );

        return response;
    }

    private Optional<ExamSchedule> findRecentPostExam(UserId userId, LocalDate today) {
        return examScheduleRepository.findFirstByUserIdAndExamDateBetweenOrderByExamDateDesc(
                userId,
                today.minusDays(7),
                today.minusDays(1)
        );
    }

    private Optional<Long> daysSinceLastCompletedSession(UserId userId, LocalDate today) {
        return focusSessionRepository.findFirstByUserIdAndEndedAtIsNotNullOrderByEndedAtDesc(userId)
                .map(FocusSession::getEndedAt)
                .map(endedAt -> endedAt.atZone(clock.getZone()).toLocalDate())
                .map(lastSessionDate -> ChronoUnit.DAYS.between(lastSessionDate, today));
    }

    private RecoveryCardResponse withPostExamMode(
            RecoveryCardResponse response,
            Optional<ExamSchedule> recentPostExam,
            LocalDate today
    ) {
        if (recentPostExam.isEmpty()) {
            return response;
        }

        ExamSchedule examSchedule = recentPostExam.get();
        return RecoveryCardResponse.builder()
                .needsRecovery(response.needsRecovery())
                .recoveryType(response.recoveryType())
                .suggestedAction(response.suggestedAction())
                .suggestedDurationMinutes(response.suggestedDurationMinutes())
                .yesterdayFocusSeconds(response.yesterdayFocusSeconds())
                .yesterdayTopPickCompletionRate(response.yesterdayTopPickCompletionRate())
                .postExamMode(true)
                .recentExamScheduleId(examSchedule.getExamScheduleId().id())
                .recentExamType(examSchedule.getExamType())
                .recentExamTitle(examSchedule.getTitle())
                .recentExamDate(examSchedule.getExamDate())
                .recentExamSubject(examSchedule.getSubject())
                .daysSinceRecentExam(ChronoUnit.DAYS.between(examSchedule.getExamDate(), today))
                .daysSinceLastSession(response.daysSinceLastSession())
                .build();
    }

    private Map<String, String> analyticsProperties(RecoveryCardResponse response, LocalDate targetDate) {
        Map<String, String> properties = new HashMap<>();
        properties.put("recoveryType", response.recoveryType().name());
        properties.put("needsRecovery", String.valueOf(response.needsRecovery()));
        properties.put("targetDate", targetDate.toString());
        properties.put("postExamMode", String.valueOf(response.postExamMode()));

        if (response.recentExamScheduleId() != null) {
            properties.put("recentExamScheduleId", response.recentExamScheduleId().toString());
            properties.put("daysSinceRecentExam", String.valueOf(response.daysSinceRecentExam()));
        }
        if (response.daysSinceLastSession() != null) {
            properties.put("daysSinceLastSession", String.valueOf(response.daysSinceLastSession()));
        }
        if (response.suggestedDurationMinutes() != null) {
            properties.put("suggestedDurationMinutes", String.valueOf(response.suggestedDurationMinutes()));
        }

        return properties;
    }
}
