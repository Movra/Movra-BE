package com.example.movra.bc.account.user.application.user;

import com.example.movra.bc.account.user.application.user.dto.request.OauthCompleteRequest;
import com.example.movra.bc.account.user.application.user.dto.response.OauthCompleteResponse;
import com.example.movra.bc.account.user.application.user.exception.OauthCallbackNotFoundException;
import com.example.movra.bc.account.user.application.user.exception.UserNotFoundException;
import com.example.movra.bc.account.user.domain.user.User;
import com.example.movra.bc.account.user.domain.user.repository.UserRepository;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.JwtTokenProvider;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.dto.OauthCallbackPayload;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.dto.PendingOauth;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.pending.OauthCallbackStore;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.pending.PendingOauthStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OauthCompleteService {

    private final OauthCallbackStore oauthCallbackStore;
    private final PendingOauthStore pendingOauthStore;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public OauthCompleteResponse complete(OauthCompleteRequest request) {
        OauthCallbackPayload payload = oauthCallbackStore.consume(request.code())
                .orElseThrow(OauthCallbackNotFoundException::new);

        if (payload.type() == null) {
            throw new OauthCallbackNotFoundException();
        }

        return switch (payload.type()) {
            case EXISTING_USER -> completeExistingUser(payload);
            case NEW_USER -> completeNewUser(payload);
        };
    }

    private OauthCompleteResponse completeExistingUser(OauthCallbackPayload payload) {
        if (payload.userId() == null) {
            throw new OauthCallbackNotFoundException();
        }

        User user = userRepository.findById(UserId.of(payload.userId()))
                .orElseThrow(UserNotFoundException::new);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId().id());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().id());

        jwtTokenProvider.save(user.getId().id().toString(), refreshToken);

        return OauthCompleteResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isProfileCompleted(true)
                .build();
    }

    private OauthCompleteResponse completeNewUser(OauthCallbackPayload payload) {
        if (payload.email() == null || payload.oauthProvider() == null) {
            throw new OauthCallbackNotFoundException();
        }

        String pendingToken = pendingOauthStore.save(
                PendingOauth.builder()
                        .email(payload.email())
                        .oauthProvider(payload.oauthProvider())
                        .build()
        );

        return OauthCompleteResponse.builder()
                .pendingToken(pendingToken)
                .isProfileCompleted(false)
                .build();
    }
}
