package com.example.movra.bc.visioning.future_vision.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.visioning.future_vision.application.exception.FutureVisionNotFoundException;
import com.example.movra.bc.visioning.future_vision.application.service.dto.response.FutureVisionResponse;
import com.example.movra.bc.visioning.future_vision.application.service.dto.response.WeeklyVisionResponse;
import com.example.movra.bc.visioning.future_vision.application.service.dto.response.YearlyVisionResponse;
import com.example.movra.bc.visioning.future_vision.domain.FutureVision;
import com.example.movra.bc.visioning.future_vision.domain.repository.FutureVisionRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QueryFutureVisionService {

    private final FutureVisionRepository futureVisionRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public FutureVisionResponse query() {
        return FutureVisionResponse.from(getFutureVisionOrThrow());
    }

    @Transactional(readOnly = true)
    public WeeklyVisionResponse queryWeeklyVision() {
        return WeeklyVisionResponse.from(getFutureVisionOrThrow());
    }

    @Transactional(readOnly = true)
    public YearlyVisionResponse queryYearlyVision() {
        return YearlyVisionResponse.from(getFutureVisionOrThrow());
    }

    @Transactional(readOnly = true)
    public Optional<FutureVisionResponse> findForHome() {
        return futureVisionRepository.findByUserId(currentUserId())
                .map(FutureVisionResponse::from);
    }

    private FutureVision getFutureVisionOrThrow() {
        return futureVisionRepository.findByUserId(currentUserId())
                .orElseThrow(FutureVisionNotFoundException::new);
    }

    private UserId currentUserId() {
        return currentUserQuery.currentUser().userId();
    }
}
