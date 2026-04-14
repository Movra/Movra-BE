package com.example.movra.bc.account.user.application.user.helper;

import com.example.movra.bc.account.user.domain.user.User;
import com.example.movra.bc.account.user.domain.user.repository.UserRepository;
import com.example.movra.bc.account.user.domain.user.type.OauthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserPersister {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User saveLocalUser(String accountId, String profileName, String profileUrl, String email, String password){
        return userRepository.save(
                User.createLocalUser(
                        accountId,
                        profileName,
                        profileUrl,
                        email,
                        passwordEncoder.encode(password)
                )
        );
    }

    @Transactional
    public User saveOauthUser(String accountId, String profileName, String profileUrl, String email, OauthProvider oauthProvider, String password){
        return userRepository.save(
                User.createOauthUser(
                        accountId,
                        profileName,
                        profileUrl,
                        email,
                        oauthProvider,
                        passwordEncoder.encode(password)
                )
        );
    }
}
