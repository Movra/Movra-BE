package com.example.movra.bc.account.user.application.user.dto.response;

import com.example.movra.bc.account.user.domain.user.AuthCredential;
import com.example.movra.bc.account.user.domain.user.User;
import com.example.movra.bc.account.user.domain.user.type.OauthProvider;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ProfileResponse(
        UUID userId,
        String accountId,
        String profileName,
        String profileImage,
        List<CredentialResponse> credentials
) {
    public static ProfileResponse from(User user) {
        List<CredentialResponse> credentials = user.getAuthCredentials().stream()
                .map(CredentialResponse::from)
                .toList();

        return ProfileResponse.builder()
                .userId(user.getId().id())
                .accountId(user.getAccountId())
                .profileName(user.getProfileName())
                .profileImage(user.getProfileImage())
                .credentials(credentials)
                .build();
    }

    @Builder
    public record CredentialResponse(
            String email,
            OauthProvider provider
    ) {
        public static CredentialResponse from(AuthCredential credential) {
            return CredentialResponse.builder()
                    .email(credential.getEmail())
                    .provider(credential.getOauthProvider())
                    .build();
        }
    }
}
