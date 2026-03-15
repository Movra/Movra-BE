package com.example.morva.bc.account.application.user;

import com.example.morva.bc.account.application.user.dto.request.OauthProfileSetupRequest;
import com.example.morva.bc.account.application.user.dto.response.ProfileSetupResponse;
import com.example.morva.bc.account.application.user.dto.response.TokenResponse;
import com.example.morva.bc.account.application.user.exception.DuplicateAccountIdException;
import com.example.morva.bc.account.application.user.exception.DuplicateUserException;
import com.example.morva.bc.account.application.user.exception.PendingOauthNotFoundException;
import com.example.morva.bc.account.application.user.exception.UserCreationFailedException;
import com.example.morva.bc.account.application.user.helper.UserPersister;
import com.example.morva.bc.account.domain.user.User;
import com.example.morva.bc.account.domain.user.repository.UserRepository;
import com.example.morva.bc.account.infrastructure.user.security.jwt.JwtTokenProvider;
import com.example.morva.bc.account.infrastructure.user.security.oauth.dto.PendingOauth;
import com.example.morva.bc.account.infrastructure.user.security.oauth.pending.PendingOauthStore;
import com.example.morva.sharedkernel.file.storage.ImageFileStorageService;
import com.example.morva.sharedkernel.file.storage.type.ImageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OauthProfileSetupService {

    private final UserRepository userRepository;
    private final PendingOauthStore pendingOauthStore;
    private final ImageFileStorageService imageFileStorageService;
    private final UserPersister userPersister;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public ProfileSetupResponse setup(String token, OauthProfileSetupRequest oauthProfileSetupRequest){
        PendingOauth pendingOauth = pendingOauthStore.find(token)
                .orElseThrow(PendingOauthNotFoundException::new);

        if(userRepository.existsByAccountId(oauthProfileSetupRequest.accountId())){
            throw new DuplicateAccountIdException();
        }

        if(userRepository.existsByAuthCredentialEmailAndProvider(pendingOauth.email(), pendingOauth.oauthProvider())){
            throw new DuplicateUserException();
        }

        String profileUrl = imageFileStorageService.upload(oauthProfileSetupRequest.profileImage(), ImageType.PROFILE);

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
            cleanupUploadedImage(profileUrl);
            throw new UserCreationFailedException();
        }

    }

    private void cleanupUploadedImage(String profileUrl){
        try{
            imageFileStorageService.deleteByKey(profileUrl);
        } catch (Exception e){
            //Exception 발생해도 그냥 진행
        }
    }
}
