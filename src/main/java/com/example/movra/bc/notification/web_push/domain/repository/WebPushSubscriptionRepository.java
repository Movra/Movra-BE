package com.example.movra.bc.notification.web_push.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.notification.web_push.domain.WebPushSubscription;
import com.example.movra.bc.notification.web_push.domain.vo.WebPushSubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebPushSubscriptionRepository extends JpaRepository<WebPushSubscription, WebPushSubscriptionId> {

    Optional<WebPushSubscription> findByEndpointHash(String endpointHash);

    List<WebPushSubscription> findAllByUserId(UserId userId);

    List<WebPushSubscription> findAllByUserIdAndRevokedAtIsNull(UserId userId);
}
