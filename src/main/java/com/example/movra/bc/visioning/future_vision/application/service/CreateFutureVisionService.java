package com.example.movra.bc.visioning.future_vision.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.visioning.future_vision.application.exception.FutureVisionAlreadyExistsException;
import com.example.movra.bc.visioning.future_vision.application.exception.FutureVisionCreationFailedException;
import com.example.movra.bc.visioning.future_vision.application.helper.FutureVisionPersister;
import com.example.movra.bc.visioning.future_vision.application.service.dto.request.CreateFutureVisionRequest;
import com.example.movra.bc.visioning.future_vision.domain.FutureVision;
import com.example.movra.bc.visioning.future_vision.domain.repository.FutureVisionRepository;
import com.example.movra.sharedkernel.file.storage.ImageHelper;
import com.example.movra.sharedkernel.file.storage.type.ImageType;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateFutureVisionService {

    private final FutureVisionPersister futureVisionPersister;
    private final FutureVisionRepository futureVisionRepository;
    private final CurrentUserQuery currentUserQuery;
    private final ImageHelper imageHelper;
    private final AnalyticsEventRecorder analyticsEventRecorder;

    @Transactional
    public void create(CreateFutureVisionRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();

        if (futureVisionRepository.existsByUserId(userId)) {
            throw new FutureVisionAlreadyExistsException();
        }

        String weeklyVisionImageUrl = imageHelper.upload(request.weeklyVisionImageUrl(), ImageType.FUTURE);
        String yearlyVisionImageUrl = imageHelper.upload(request.yearlyVisionImageUrl(), ImageType.FUTURE);

        try{
            FutureVision futureVision = futureVisionPersister.saveFutureVision(
                    userId,
                    weeklyVisionImageUrl,
                    yearlyVisionImageUrl,
                    request.yearlyVisionDescription()
            );
            analyticsEventRecorder.recordSafely(
                    userId,
                    AnalyticsEventType.FUTURE_VISION_CREATED,
                    Map.of("futureVisionId", futureVision.getId().id().toString())
            );
        } catch (Exception e) {
            imageHelper.cleanup(weeklyVisionImageUrl);
            imageHelper.cleanup(yearlyVisionImageUrl);
            log.error("FutureVision 실패: {}", e.getMessage());
            throw new FutureVisionCreationFailedException();
        }
    }
}
