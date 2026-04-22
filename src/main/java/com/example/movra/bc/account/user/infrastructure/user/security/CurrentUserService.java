package com.example.movra.bc.account.user.infrastructure.user.security;

import com.example.movra.bc.account.user.domain.user.User;
import com.example.movra.bc.account.user.infrastructure.user.security.auth.AuthDetails;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.exception.InvalidJwtException;
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
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AuthDetails authDetails)) {
            throw new InvalidJwtException();
        }

        User detachedUser = authDetails.getUser();

        return AuthenticatedUser.builder()
                .userId(detachedUser.getId())
                .accountId(detachedUser.getAccountId())
                .name(detachedUser.getProfileName())
                .build();
    }
}
