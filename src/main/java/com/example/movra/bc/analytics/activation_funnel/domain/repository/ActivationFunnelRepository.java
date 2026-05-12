package com.example.movra.bc.analytics.activation_funnel.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_funnel.domain.ActivationFunnel;
import com.example.movra.bc.analytics.activation_funnel.domain.vo.ActivationFunnelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivationFunnelRepository extends JpaRepository<ActivationFunnel, ActivationFunnelId> {

    Optional<ActivationFunnel> findByUserId(UserId userId);
}
