package com.example.movra.bc.focus.focus_session.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * 특정 날짜에 활동한 모든 유저의 집중 세션을 일괄 마감하여
 * {@code DailyFocusSummary} 읽기모델(스냅샷)을 생성한다.
 * <p>
 * 감시자(watcher) 조회는 이 스냅샷을 읽으므로, 이 배치가 실행돼야
 * 감시자가 친구의 마감된 하루 데이터를 볼 수 있다.
 * <p>
 * 유저별 마감은 {@link DailyFocusCloser#close}가 멱등하게 처리하므로
 * 한 유저가 실패해도 전체 배치를 중단하지 않고 계속 진행한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyFocusClosingService {

    private final DailyFocusSummaryReader dailyFocusSummaryReader;
    private final DailyFocusCloser dailyFocusCloser;
    private final Clock clock;

    public int closeYesterday() {
        return closeFor(LocalDate.now(clock).minusDays(1));
    }

    public int closeFor(LocalDate date) {
        List<UserId> userIds = dailyFocusSummaryReader.findActiveUserIds(date);
        log.info("[DailyFocusClosing] start date={}, activeUsers={}", date, userIds.size());

        int closedCount = 0;
        for (UserId userId : userIds) {
            try {
                dailyFocusCloser.close(userId, date);
                closedCount++;
            } catch (Exception e) {
                log.error("[DailyFocusClosing] failed user={}, date={}", userId.id(), date, e);
            }
        }

        log.info("[DailyFocusClosing] done date={}, processed={}/{}", date, closedCount, userIds.size());
        return closedCount;
    }
}
