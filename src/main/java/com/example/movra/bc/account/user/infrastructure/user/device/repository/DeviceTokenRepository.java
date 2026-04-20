package com.example.movra.bc.account.user.infrastructure.user.device.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.account.user.infrastructure.user.device.DeviceToken;
import com.example.movra.bc.account.user.infrastructure.user.device.DeviceTokenId;
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
