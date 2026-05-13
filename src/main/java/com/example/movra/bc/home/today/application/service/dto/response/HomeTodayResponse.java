package com.example.movra.bc.home.today.application.service.dto.response;

import com.example.movra.bc.notification.application.service.dto.response.NotificationPreferenceResponse;
import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.dto.response.TopPicksResponse;
import com.example.movra.bc.planning.exam_schedule.application.service.dto.response.ExamScheduleResponse;
import com.example.movra.bc.planning.exam_schedule.domain.type.SeasonMode;
import com.example.movra.bc.planning.timetable.application.service.dto.response.TimetableResponse;
import com.example.movra.bc.visioning.future_vision.application.service.dto.response.FutureVisionResponse;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record HomeTodayResponse(
        LocalDate targetDate,
        FutureVisionResponse futureVision,
        List<TopPicksResponse> topPicks,
        TimetableResponse timetable,
        SeasonMode seasonMode,
        ExamScheduleResponse nextExamSchedule,
        NotificationPreferenceResponse notificationPreference,
        FriendAccountabilityStatusResponse friendAccountability,
        boolean showFocusTimingCard
) {
}
