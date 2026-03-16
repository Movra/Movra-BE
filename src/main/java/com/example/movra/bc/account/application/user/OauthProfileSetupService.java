package com.example.movra.bc.account.application.user;

import com.example.movra.bc.account.application.user.dto.request.OauthProfileSetupRequest;
import com.example.movra.bc.account.application.user.dto.response.ProfileSetupResponse;
import com.example.movra.bc.account.application.user.exception.DuplicateAccountIdException;
import com.example.movra.bc.account.application.user.exception.DuplicateUserException;
import com.example.movra.bc.account.application.user.exception.PendingOauthNotFoundException;
import com.example.movra.bc.account.application.user.exception.UserCreationFailedException;
import com.example.movra.bc.account.application.user.helper.ProfileImageHelper;
import com.example.movra.bc.account.application.user.helper.UserPersister;
import com.example.movra.bc.account.domain.user.User;
import com.example.movra.bc.account.domain.user.repository.UserRepository;
import com.example.movra.bc.account.infrastructure.user.security.jwt.JwtTokenProvider;
import com.example.movra.bc.account.infrastructure.user.security.oauth.dto.PendingOauth;
import com.example.movra.bc.account.infrastructure.user.security.oauth.pending.PendingOauthStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OauthProfileSetupService {

    private final UserRepository userRepository;
    private final PendingOauthStore pendingOauthStore;
    private final ProfileImageHelper profileImageHelper;
    private final UserPersister userPersister;
    private final JwtTokenProvider jwtTokenProvider;

    public ProfileSetupResponse setup(String token, OauthProfileSetupRequest oauthProfileSetupRequest){
        PendingOauth pendingOauth = pendingOauthStore.find(token)
                .orElseThrow(PendingOauthNotFoundException::new);

        if(userRepository.existsByAccountId(oauthProfileSetupRequest.accountId())){
            throw new DuplicateAccountIdException();
        }

        if(userRepository.existsByAuthCredentialEmailAndProvider(pendingOauth.email(), pendingOauth.oauthProvider())){
            throw new DuplicateUserException();
        }

        String profileUrl = profileImageHelper.upload(oauthProfileSetupRequest.profileImage());

        try{
            User user = userPersister.saveOauthUser(
                            oauthProfileSetupRequest.accountId(),
                            oauthProfileSetupRequest.profileName(),
                            profileUrl,
                            pendingOauth.email(),
                            pendingOauth.oauthProvider(),
                            oauthProfileSetupRequest.password()
                    );

            String accessToken = jwtTokenProvider.generateAccessToken(user.getId().id());
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().id());

            jwtTokenProvider.save(user.getId().id().toString(), refreshToken);
            pendingOauthStore.remove(token);

            return ProfileSetupResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .isProfileCompleted(true)
                    .build();
        } catch (Exception e){
            profileImageHelper.cleanup(profileUrl);
            throw new UserCreationFailedException();
        }
    }
}
