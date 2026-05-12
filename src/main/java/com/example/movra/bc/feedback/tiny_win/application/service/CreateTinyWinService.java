package com.example.movra.bc.feedback.tiny_win.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.feedback.tiny_win.application.service.dto.request.TinyWinRequest;
import com.example.movra.bc.feedback.tiny_win.domain.TinyWin;
import com.example.movra.bc.feedback.tiny_win.domain.repository.TinyWinRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CreateTinyWinService {

    private final TinyWinRepository tinyWinRepository;
    private final CurrentUserQuery currentUserQuery;
    private final AnalyticsEventRecorder analyticsEventRecorder;

    @Transactional
    public void create(TinyWinRequest request){
        UserId userId = currentUserQuery.currentUser().userId();
        TinyWin tinyWin = tinyWinRepository.save(
                TinyWin.create(
                        userId,
                        request.title(),
                        request.content()
                )
        );
        analyticsEventRecorder.recordSafely(
                userId,
                AnalyticsEventType.TINY_WIN_CREATED,
                Map.of("tinyWinId", tinyWin.getId().id().toString())
        );
    }
}
