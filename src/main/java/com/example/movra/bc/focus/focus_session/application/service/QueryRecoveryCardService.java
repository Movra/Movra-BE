package com.example.movra.bc.focus.focus_session.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.focus.focus_session.application.service.dto.response.RecoveryCardResponse;
import com.example.movra.bc.focus.focus_session.domain.DailyFocusSummary;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.repository.DailyFocusSummaryRepository;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import com.example.movra.bc.focus.focus_session.domain.type.RecoveryType;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.BehaviorProfileRepository;
import com.example.movra.bc.personalization.behavior_profile.domain.type.CoachingMode;
import com.example.movra.bc.personalization.behavior_profile.domain.type.RecoveryStyle;
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
        RecoveryType recoveryType = determineRecoveryType(missedFocus, incompleteTopPick, recentPostExam, daysSinceLastSession);
        Optional<BehaviorProfile> behaviorProfile = behaviorProfileRepository.findByUserId(userId);

        RecoveryCardResponse response = RecoveryCardResponse.builder()
                .needsRecovery(recoveryType != RecoveryType.NONE)
                .recoveryType(recoveryType)
                .suggestedAction(determineSuggestedAction(behaviorProfile, recoveryType))
                .suggestedDurationMinutes(determineSuggestedDurationMinutes(behaviorProfile, recoveryType))
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

    private RecoveryType determineRecoveryType(
            boolean missedFocus,
            boolean incompleteTopPick,
            Optional<ExamSchedule> recentPostExam,
            Optional<Long> daysSinceLastSession
    ) {
        if (recentPostExam.isPresent()) {
            return RecoveryType.POST_EXAM_RECOVERY;
        }
        if (daysSinceLastSession.map(days -> days >= 7).orElse(false)) {
            return RecoveryType.LONG_ABSENCE;
        }
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

    private String determineSuggestedAction(Optional<BehaviorProfile> behaviorProfile, RecoveryType recoveryType) {
        if (recoveryType == RecoveryType.POST_EXAM_RECOVERY) {
            return "시험 직후에는 회복이 먼저예요. 오늘은 10분만 가볍게 다시 시작해볼까요?";
        }

        if (recoveryType == RecoveryType.LONG_ABSENCE) {
            return "오랜만이어도 괜찮아요. 오늘은 3분만 다시 연결해볼까요?";
        }

        if (recoveryType == RecoveryType.NONE) {
            return null;
        }

        RecoveryStyle recoveryStyle = behaviorProfile.map(BehaviorProfile::getRecoveryStyle).orElse(null);
        CoachingMode coachingMode = behaviorProfile.map(BehaviorProfile::getCoachingMode).orElse(CoachingMode.NEUTRAL);

        if (recoveryStyle == null) {
            return defaultMessageFor(coachingMode);
        }

        return messageFor(recoveryStyle, coachingMode);
    }

    private Integer determineSuggestedDurationMinutes(Optional<BehaviorProfile> behaviorProfile, RecoveryType recoveryType) {
        if (recoveryType == RecoveryType.NONE) {
            return null;
        }

        if (recoveryType == RecoveryType.POST_EXAM_RECOVERY) {
            return 10;
        }

        if (recoveryType == RecoveryType.LONG_ABSENCE) {
            return 3;
        }

        return behaviorProfile.map(BehaviorProfile::getRecoveryStyle)
                .map(this::durationForRecoveryStyle)
                .orElse(5);
    }

    private int durationForRecoveryStyle(RecoveryStyle recoveryStyle) {
        return switch (recoveryStyle) {
            case QUICK_RESTART -> 5;
            case NEEDS_REFLECTION -> 5;
            case SLOW_REBUILDER -> 3;
        };
    }

    private String messageFor(RecoveryStyle recoveryStyle, CoachingMode coachingMode) {
        return switch (recoveryStyle) {
            case QUICK_RESTART -> switch (coachingMode) {
                case GENTLE -> "어제는 쉬어갔어요. 준비됐을 때 가볍게 시작해볼까요?";
                case STRICT -> "준비됐어? 지금 바로 5분만 가자.";
                case NEUTRAL -> "어제는 쉬어갔어요. 지금 바로 시작해볼까요?";
            };
            case NEEDS_REFLECTION -> switch (coachingMode) {
                case GENTLE -> "어제 무엇이 힘들었는지 천천히 한 줄만 적어봐도 좋아요.";
                case STRICT -> "어제 무엇이 무너졌는지 한 줄로 정리하고 다시 시작해.";
                case NEUTRAL -> "어제 무엇이 어려웠는지 한 줄만 적어볼까요?";
            };
            case SLOW_REBUILDER -> switch (coachingMode) {
                case GENTLE -> "오늘은 3분이면 충분해요. 천천히 다시 연결해볼까요?";
                case STRICT -> "3분만. 그 정도는 지금 할 수 있어.";
                case NEUTRAL -> "3분만 해볼까요? 작게 시작하면 돼요.";
            };
        };
    }

    private String defaultMessageFor(CoachingMode coachingMode) {
        return switch (coachingMode) {
            case GENTLE -> "괜찮아요. 오늘 다시 시작해볼까요?";
            case STRICT -> "다시 시작해. 지금이 그 시점이야.";
            case NEUTRAL -> "다시 시작해볼까요?";
        };
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
