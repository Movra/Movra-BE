package com.example.movra.bc.account.user.application.user.internal;

import com.example.movra.bc.account.user.domain.user.User;
import com.example.movra.bc.account.user.domain.user.repository.UserRepository;
import com.example.movra.bc.account.user.domain.user.type.OauthProvider;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.dto.PendingOauth;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.pending.PendingOauthStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OauthUserService {

    private final UserRepository userRepository;
    private final PendingOauthStore pendingOauthStore;

    @Transactional(readOnly = true)
    public Optional<User> findByEmailAndProvider(String email, OauthProvider oauthProvider) {
        return userRepository.findByAuthCredentialEmailAndProvider(email, oauthProvider);
    }

    @Transactional
    public String stagePendingOauth(String email, OauthProvider oauthProvider){
        return pendingOauthStore.save(
                PendingOauth.builder()
                        .email(email)
                        .oauthProvider(oauthProvider)
                        .build()
        );
    }
}
