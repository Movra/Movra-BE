package com.example.movra.bc.planning.timetable.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * 특정 날짜에 일일 계획이 있던 모든 유저의 타임테이블을 일괄 마감하여
 * {@code DailyTimetableSummary} 읽기모델(스냅샷)을 생성한다.
 * <p>
 * 감시자(watcher)의 타임테이블 조회는 이 스냅샷을 읽으므로 이 배치가 선행돼야 한다.
 * 유저별 마감은 {@link DailyTimetableCloser#close}가 멱등하게 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyTimetableClosingService {

    private final DailyPlanRepository dailyPlanRepository;
    private final DailyTimetableCloser dailyTimetableCloser;
    private final Clock clock;

    public int closeYesterday() {
        return closeFor(LocalDate.now(clock).minusDays(1));
    }

    public int closeFor(LocalDate date) {
        List<UserId> userIds = dailyPlanRepository.findDistinctUserIdsByPlanDate(date);
        log.info("[DailyTimetableClosing] start date={}, activeUsers={}", date, userIds.size());

        int closedCount = 0;
        for (UserId userId : userIds) {
            try {
                dailyTimetableCloser.close(userId, date);
                closedCount++;
            } catch (Exception e) {
                log.error("[DailyTimetableClosing] failed user={}, date={}", userId.id(), date, e);
            }
        }

        log.info("[DailyTimetableClosing] done date={}, processed={}/{}", date, closedCount, userIds.size());
        return closedCount;
    }
}
