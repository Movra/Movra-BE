package com.example.movra.bc.visioning.future_vision.application.service;

import com.example.movra.bc.account.domain.user.vo.UserId;
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

@Service
@RequiredArgsConstructor
public class QueryFutureVisionService {

    private final FutureVisionRepository futureVisionRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public FutureVisionResponse query() {
        return FutureVisionResponse.from(getFutureVision());
    }

    @Transactional(readOnly = true)
    public WeeklyVisionResponse queryWeeklyVision() {
        return WeeklyVisionResponse.from(getFutureVision());
    }

    @Transactional(readOnly = true)
    public YearlyVisionResponse queryYearlyVision() {
        return YearlyVisionResponse.from(getFutureVision());
    }

    private FutureVision getFutureVision() {
        UserId userId = currentUserQuery.currentUser().userId();

        return futureVisionRepository.findByUserId(userId)
                .orElseThrow(FutureVisionNotFoundException::new);
    }
}
