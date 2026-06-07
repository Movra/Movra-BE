package com.example.movra.bc.focus.focus_session.application.schedule;

import com.example.movra.bc.focus.focus_session.application.service.support.FocusSessionAutoCloseService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 주기적으로 상한 시간을 넘긴 진행 중 집중 세션을 자동 마감한다.
 * stop을 누르지 않아 영원히 진행 중으로 남는 세션이 새 세션 시작을 막는 것을 방지한다.
 */
@Component
@RequiredArgsConstructor
public class FocusSessionAutoCloseScheduler {

    private final FocusSessionAutoCloseService focusSessionAutoCloseService;

    @Scheduled(cron = "${app.focus.auto-close.cron:0 */10 * * * *}", zone = "${app.time.zone:Asia/Seoul}")
    public void closeExpiredSessions() {
        focusSessionAutoCloseService.closeExpiredSessions();
    }
}
