package com.example.morva.bc.account.application.user.internal;

import com.example.morva.bc.account.domain.user.User;
import com.example.morva.bc.account.domain.user.repository.UserRepository;
import com.example.morva.bc.account.domain.user.type.OauthProvider;
import com.example.morva.bc.account.infrastructure.user.security.oauth.dto.PendingOauth;
import com.example.morva.bc.account.infrastructure.user.security.oauth.pending.PendingOauthStore;
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
