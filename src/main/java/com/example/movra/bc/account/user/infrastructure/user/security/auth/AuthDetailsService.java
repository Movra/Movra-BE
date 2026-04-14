package com.example.movra.bc.account.user.infrastructure.user.security.auth;

import com.example.movra.bc.account.user.domain.user.repository.UserRepository;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        try {
            return userRepository.findById(UserId.of(UUID.fromString(userId)))
                    .map(AuthDetails::new)
                    .orElseThrow(() -> new UsernameNotFoundException(userId));
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException(userId);
        }
    }
}
