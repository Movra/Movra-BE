package com.example.movra.infrastructure.account.user.security;

import com.example.movra.bc.account.user.domain.user.User;
import com.example.movra.bc.account.user.infrastructure.user.security.CurrentUserService;
import com.example.movra.bc.account.user.infrastructure.user.security.auth.AuthDetails;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.exception.InvalidJwtException;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrentUserServiceTest {

    private final CurrentUserService currentUserService = new CurrentUserService();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("currentUser returns authenticated user when authentication is valid")
    void currentUser_authenticatedAuthentication_returnsUser() {
        User user = User.createLocalUser("tester", "tester", "image", "tester@example.com", "pw");
        AuthDetails authDetails = new AuthDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authDetails, null, authDetails.getAuthorities())
        );

        AuthenticatedUser authenticatedUser = currentUserService.currentUser();

        assertThat(authenticatedUser.userId()).isEqualTo(user.getId());
        assertThat(authenticatedUser.accountId()).isEqualTo(user.getAccountId());
        assertThat(authenticatedUser.name()).isEqualTo(user.getProfileName());
    }

    @Test
    @DisplayName("currentUser throws when authentication is not authenticated")
    void currentUser_unauthenticatedAuthentication_throwsException() {
        User user = User.createLocalUser("tester", "tester", "image", "tester@example.com", "pw");
        AuthDetails authDetails = new AuthDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authDetails, null)
        );

        assertThatThrownBy(() -> currentUserService.currentUser())
                .isInstanceOf(InvalidJwtException.class);
    }
}
