package com.example.movra.bc.focus.focus_session.presentation;

import com.example.movra.bc.focus.focus_session.application.service.QueryRecoveryCardService;
import com.example.movra.bc.focus.focus_session.application.service.QueryTodayFocusSessionsService;
import com.example.movra.bc.focus.focus_session.application.service.RecordRecoveryCardActionService;
import com.example.movra.bc.focus.focus_session.application.service.StartFocusSessionService;
import com.example.movra.bc.focus.focus_session.application.service.StopFocusSessionService;
import com.example.movra.bc.focus.focus_session.application.service.dto.request.RecordRecoveryCardActionRequest;
import com.example.movra.bc.focus.focus_session.application.service.dto.request.StartFocusSessionRequest;
import com.example.movra.bc.focus.focus_session.application.service.dto.response.FocusSessionResponse;
import com.example.movra.bc.focus.focus_session.application.service.dto.response.RecoveryCardResponse;
import com.example.movra.bc.focus.focus_session.application.service.dto.response.TodayFocusSessionsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/focus-sessions")
@RequiredArgsConstructor
public class FocusSessionController {

    private final StartFocusSessionService startFocusSessionService;
    private final StopFocusSessionService stopFocusSessionService;
    private final QueryTodayFocusSessionsService queryTodayFocusSessionsService;
    private final QueryRecoveryCardService queryRecoveryCardService;
    private final RecordRecoveryCardActionService recordRecoveryCardActionService;

    @PostMapping("/start")
    public FocusSessionResponse start(@Valid @RequestBody StartFocusSessionRequest request) {
        return startFocusSessionService.start(request);
    }

    @PatchMapping("/stop")
    public FocusSessionResponse stop() {
        return stopFocusSessionService.stop();
    }

    @GetMapping("/today")
    public TodayFocusSessionsResponse queryToday() {
        return queryTodayFocusSessionsService.query();
    }

    @GetMapping("/recovery-card")
    public RecoveryCardResponse queryRecoveryCard() {
        return queryRecoveryCardService.query();
    }

    @PostMapping("/recovery-card/actions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recordRecoveryCardAction(@Valid @RequestBody RecordRecoveryCardActionRequest request) {
        recordRecoveryCardActionService.record(request);
    }
}
