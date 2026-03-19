package com.example.movra.bc.account.infrastructure.user.security;

import com.example.movra.bc.account.domain.user.User;
import com.example.movra.bc.account.infrastructure.user.security.auth.AuthDetails;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserService implements CurrentUserQuery {


    @Override
    public AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        AuthDetails authDetails = (AuthDetails) authentication.getPrincipal();
        User detachedUser = authDetails.getUser();

        return AuthenticatedUser.builder()
                .userId(detachedUser.getId())
                .accountId(detachedUser.getAccountId())
                .name(detachedUser.getProfileName())
                .build();
    }
}
