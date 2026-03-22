package com.example.movra.bc.visioning.future_vision.application.service;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.visioning.future_vision.application.exception.FutureVisionAlreadyExistsException;
import com.example.movra.bc.visioning.future_vision.application.exception.FutureVisionCreationFailedException;
import com.example.movra.bc.visioning.future_vision.application.helper.FutureVisionPersister;
import com.example.movra.bc.visioning.future_vision.application.service.dto.request.CreateFutureVisionRequest;
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
public class CreateFutureVisionService {

    private final FutureVisionPersister futureVisionPersister;
    private final FutureVisionRepository futureVisionRepository;
    private final CurrentUserQuery currentUserQuery;
    private final ImageHelper imageHelper;

    @Transactional
    public void create(CreateFutureVisionRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();

        if (futureVisionRepository.existsByUserId(userId)) {
            throw new FutureVisionAlreadyExistsException();
        }

        String weeklyVisionImageUrl = imageHelper.upload(request.weeklyVisionImageUrl(), ImageType.FUTURE);
        String yearlyVisionImageUrl = imageHelper.upload(request.yearlyVisionImageUrl(), ImageType.FUTURE);

        try{
            futureVisionPersister.saveFutureVision(
                    userId,
                    weeklyVisionImageUrl,
                    yearlyVisionImageUrl,
                    request.yearlyVisionDescription()
            );
        } catch (Exception e) {
            imageHelper.cleanup(weeklyVisionImageUrl);
            imageHelper.cleanup(yearlyVisionImageUrl);
            log.error("FutureVision 실패: {}", e.getMessage());
            throw new FutureVisionCreationFailedException();
        }
    }
}
