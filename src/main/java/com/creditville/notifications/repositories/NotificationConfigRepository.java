package com.creditville.notifications.repositories;

/* Created by David on 6/10/2021 */

import com.creditville.notifications.models.Branch;
import com.creditville.notifications.models.NotificationConfig;
import com.creditville.notifications.models.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationConfigRepository extends JpaRepository<NotificationConfig, Long> {
    NotificationConfig findByNotificationTypeAndBranch(NotificationType notificationType, Branch branch);

    List<NotificationConfig> findAllByBranch(Branch branch);
}
