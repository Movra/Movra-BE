package com.example.movra.bc.notification.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.notification.domain.NotificationPreference;
import com.example.movra.bc.notification.domain.vo.NotificationPreferenceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, NotificationPreferenceId> {

    Optional<NotificationPreference> findByUserId(UserId userId);
}
