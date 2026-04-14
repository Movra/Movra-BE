package com.example.movra.bc.account.device_token.presentation;

import com.example.movra.bc.account.device_token.application.service.RegisterDeviceTokenService;
import com.example.movra.bc.account.device_token.application.service.UnregisterDeviceTokenService;
import com.example.movra.bc.account.device_token.application.service.dto.request.RegisterDeviceTokenRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/device-tokens")
@RequiredArgsConstructor
public class DeviceTokenController {

    private final RegisterDeviceTokenService registerDeviceTokenService;
    private final UnregisterDeviceTokenService unregisterDeviceTokenService;

    @PostMapping
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterDeviceTokenRequest request) {
        registerDeviceTokenService.register(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<Void> unregister(@PathVariable String token) {
        unregisterDeviceTokenService.unregister(token);
        return ResponseEntity.noContent().build();
    }
}
