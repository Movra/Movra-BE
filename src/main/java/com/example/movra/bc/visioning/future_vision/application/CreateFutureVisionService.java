package com.example.movra.bc.visioning.future_vision.application;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.visioning.future_vision.application.exception.FutureVisionAlreadyExistsException;
import com.example.movra.bc.visioning.future_vision.application.service.dto.request.CreateFutureVisionRequest;
import com.example.movra.bc.visioning.future_vision.domain.FutureVision;
import com.example.movra.bc.visioning.future_vision.domain.repository.FutureVisionRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateFutureVisionService {

    private final FutureVisionRepository futureVisionRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void create(CreateFutureVisionRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();

        if (futureVisionRepository.existsByUserId(userId)) {
            throw new FutureVisionAlreadyExistsException();
        }

        futureVisionRepository.save(FutureVision.create(
                userId,
                request.weeklyVisionImageUrl(),
                request.yearlyVisionImageUrl(),
                request.yearlyVisionDescription()
        ));
    }
}
