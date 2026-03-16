package com.example.movra.bc.account.domain.user.repository;

import com.example.movra.bc.account.domain.user.User;
import com.example.movra.bc.account.domain.user.type.OauthProvider;
import com.example.movra.bc.account.domain.user.vo.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, UserId> {
    boolean existsByAccountId(String accountId);

    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.authCredentials c WHERE c.email = :email")
    boolean existsByAuthCredentialEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.authCredentials c WHERE c.email = :email AND c.oauthProvider = :provider")
    boolean existsByAuthCredentialEmailAndProvider(@Param("email") String email, @Param("provider") OauthProvider provider);

    Optional<User> findByAccountId(String accountId);

    @Query("SELECT u FROM User u JOIN u.authCredentials c WHERE c.email = :email AND c.oauthProvider = :provider")
    Optional<User> findByAuthCredentialEmailAndProvider(@Param("email") String email, @Param("provider") OauthProvider provider);
}
