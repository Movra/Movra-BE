package com.example.movra.bc.visioning.future_vision.application.helper;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.visioning.future_vision.domain.FutureVision;
import com.example.movra.bc.visioning.future_vision.domain.repository.FutureVisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FutureVisionPersister {

    private final FutureVisionRepository futureVisionRepository;

    @Transactional
    public FutureVision saveFutureVision(UserId userId, String weeklyVisionImageUrl, String yearlyVisionImageUrl, String yearlyVisionDescription){
        return futureVisionRepository.save(
                FutureVision.create(
                        userId,
                        weeklyVisionImageUrl,
                        yearlyVisionImageUrl,
                        yearlyVisionDescription
                )
        );
    }

    @Transactional
    public void saveFutureVision(FutureVision futureVision){
        futureVisionRepository.save(futureVision);
    }
}
