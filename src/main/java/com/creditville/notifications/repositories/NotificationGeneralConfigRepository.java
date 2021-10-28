package com.creditville.notifications.repositories;

/* Created by David on 6/10/2021 */

import com.creditville.notifications.models.NotificationGeneralConfig;
import com.creditville.notifications.models.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationGeneralConfigRepository extends JpaRepository<NotificationGeneralConfig, Long> {
    NotificationGeneralConfig findByNotificationType(NotificationType notificationType);
}
