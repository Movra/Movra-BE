package com.example.movra.bc.account.device_token.application.service;

import com.example.movra.bc.account.device_token.application.exception.DeviceTokenNotFoundException;
import com.example.movra.bc.account.device_token.application.service.dto.request.UnregisterDeviceTokenRequest;
import com.example.movra.bc.account.device_token.domain.DeviceToken;
import com.example.movra.bc.account.device_token.domain.repository.DeviceTokenRepository;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnregisterDeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void unregister(UnregisterDeviceTokenRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();
        DeviceToken deviceToken = deviceTokenRepository.findByToken(request.token())
                .orElseThrow(DeviceTokenNotFoundException::new);

        if (!deviceToken.getUserId().equals(userId)) {
            throw new DeviceTokenNotFoundException();
        }

        deviceTokenRepository.deleteByToken(request.token());
    }
}
