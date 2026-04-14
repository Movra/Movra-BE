package com.example.movra.bc.account.device_token.application.service;

import com.example.movra.bc.account.device_token.application.service.dto.request.RegisterDeviceTokenRequest;
import com.example.movra.bc.account.device_token.domain.DeviceToken;
import com.example.movra.bc.account.device_token.domain.repository.DeviceTokenRepository;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class RegisterDeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;

    @Transactional
    public void register(RegisterDeviceTokenRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();

        deviceTokenRepository.findByToken(request.token())
                .ifPresentOrElse(
                        existing -> existing.reassignTo(userId, request.deviceLabel(), clock),
                        () -> deviceTokenRepository.save(
                                DeviceToken.register(userId, request.token(), request.deviceLabel(), clock))
                );
    }
}
