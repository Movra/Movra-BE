package com.example.movra.bc.account.user.infrastructure.user.security.token.repository;

import com.example.movra.bc.account.user.infrastructure.user.security.token.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
