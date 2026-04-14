package com.example.movra.bc.account.device_token.domain.repository;

import com.example.movra.bc.account.device_token.domain.DeviceToken;
import com.example.movra.bc.account.device_token.domain.vo.DeviceTokenId;
import com.example.movra.bc.account.domain.user.vo.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, DeviceTokenId> {

    List<DeviceToken> findAllByUserId(UserId userId);

    Optional<DeviceToken> findByToken(String token);

    void deleteByToken(String token);
}
