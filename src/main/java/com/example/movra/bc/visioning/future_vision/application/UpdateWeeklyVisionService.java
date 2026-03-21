package com.example.movra.bc.visioning.future_vision.application;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.visioning.future_vision.application.exception.FutureVisionNotFoundException;
import com.example.movra.bc.visioning.future_vision.application.service.dto.request.UpdateWeeklyVisionRequest;
import com.example.movra.bc.visioning.future_vision.domain.FutureVision;
import com.example.movra.bc.visioning.future_vision.domain.repository.FutureVisionRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateWeeklyVisionService {

    private final FutureVisionRepository futureVisionRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void update(UpdateWeeklyVisionRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();

        FutureVision futureVision = futureVisionRepository.findByUserId(userId)
                .orElseThrow(FutureVisionNotFoundException::new);

        futureVision.updateWeeklyVision(request.weeklyVisionImageUrl());
        futureVisionRepository.save(futureVision);
    }
}
