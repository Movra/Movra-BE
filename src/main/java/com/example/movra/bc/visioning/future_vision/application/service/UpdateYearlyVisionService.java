package com.example.movra.bc.visioning.future_vision.application.service;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.visioning.future_vision.application.exception.FutureVisionNotFoundException;
import com.example.movra.bc.visioning.future_vision.application.exception.FutureVisionUpdateFailedException;
import com.example.movra.bc.visioning.future_vision.application.helper.FutureVisionPersister;
import com.example.movra.bc.visioning.future_vision.application.service.dto.request.UpdateYearlyVisionRequest;
import com.example.movra.bc.visioning.future_vision.domain.FutureVision;
import com.example.movra.bc.visioning.future_vision.domain.repository.FutureVisionRepository;
import com.example.movra.sharedkernel.file.storage.ImageHelper;
import com.example.movra.sharedkernel.file.storage.type.ImageType;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateYearlyVisionService {

    private final FutureVisionRepository futureVisionRepository;
    private final FutureVisionPersister futureVisionPersister;
    private final ImageHelper imageHelper;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void update(UpdateYearlyVisionRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();

        FutureVision futureVision = futureVisionRepository.findByUserId(userId)
                .orElseThrow(FutureVisionNotFoundException::new);

        String oldYearlyVisionImageUrl = futureVision.getYearlyVisionImageUrl();
        String newYearlyVisionImageUrl = imageHelper.upload(request.yearlyVisionImageUrl(), ImageType.FUTURE);

        try {
            futureVision.updateYearlyVision(
                    newYearlyVisionImageUrl,
                    request.yearlyVisionDescription()
            );
            futureVisionPersister.saveFutureVision(futureVision);
            imageHelper.cleanup(oldYearlyVisionImageUrl);
        } catch (Exception e) {
            log.error("FutureVision 실패: {}", e.getMessage());
            imageHelper.cleanup(newYearlyVisionImageUrl);
            throw new FutureVisionUpdateFailedException();
        }
    }
}
