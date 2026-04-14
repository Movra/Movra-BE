package com.example.movra.bc.account.user.application.user;

import com.example.movra.bc.account.user.application.user.dto.request.OauthProfileSetupRequest;
import com.example.movra.bc.account.user.application.user.dto.response.ProfileSetupResponse;
import com.example.movra.bc.account.user.application.user.exception.DuplicateAccountIdException;
import com.example.movra.bc.account.user.application.user.exception.DuplicateUserException;
import com.example.movra.bc.account.user.application.user.exception.PendingOauthNotFoundException;
import com.example.movra.bc.account.user.application.user.exception.UserCreationFailedException;
import com.example.movra.sharedkernel.file.storage.ImageHelper;
import com.example.movra.bc.account.user.application.user.helper.UserPersister;
import com.example.movra.bc.account.user.domain.user.User;
import com.example.movra.bc.account.user.domain.user.repository.UserRepository;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.JwtTokenProvider;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.dto.PendingOauth;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.pending.PendingOauthStore;
import com.example.movra.sharedkernel.file.storage.type.ImageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OauthProfileSetupService {

    private final UserRepository userRepository;
    private final PendingOauthStore pendingOauthStore;
    private final ImageHelper imageHelper;
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

        String profileUrl = imageHelper.upload(oauthProfileSetupRequest.profileImage(), ImageType.PROFILE);

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
            imageHelper.cleanup(profileUrl);
            throw new UserCreationFailedException();
        }
    }
}
