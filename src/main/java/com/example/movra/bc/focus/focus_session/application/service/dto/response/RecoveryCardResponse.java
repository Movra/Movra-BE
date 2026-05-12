package com.example.movra.bc.focus.focus_session.application.service.dto.response;

import com.example.movra.bc.focus.focus_session.domain.type.RecoveryType;
import com.example.movra.bc.planning.exam_schedule.domain.type.ExamType;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record RecoveryCardResponse(
        boolean needsRecovery,
        RecoveryType recoveryType,
        String suggestedAction,
        Integer suggestedDurationMinutes,
        long yesterdayFocusSeconds,
        double yesterdayTopPickCompletionRate,
        boolean postExamMode,
        UUID recentExamScheduleId,
        ExamType recentExamType,
        String recentExamTitle,
        LocalDate recentExamDate,
        String recentExamSubject,
        Long daysSinceRecentExam,
        Long daysSinceLastSession
) {
}
