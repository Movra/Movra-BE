package com.example.movra.bc.notification.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.notification.domain.NotificationDeliveryLog;
import com.example.movra.bc.notification.domain.vo.NotificationDeliveryLogId;
import com.example.movra.sharedkernel.notification.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface NotificationDeliveryLogRepository extends JpaRepository<NotificationDeliveryLog, NotificationDeliveryLogId> {

    long countByUserIdAndSentDate(UserId userId, LocalDate sentDate);

    long countByUserIdAndNotificationTypeAndSentDate(UserId userId, NotificationType notificationType, LocalDate sentDate);
}
