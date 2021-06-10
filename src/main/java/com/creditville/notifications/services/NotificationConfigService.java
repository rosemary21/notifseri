package com.creditville.notifications.services;

/* Created by David on 6/10/2021 */

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.NotificationConfig;
import com.creditville.notifications.models.NotificationGeneralConfig;

import java.util.List;

public interface NotificationConfigService {
    NotificationConfig createNew(Long branchId, String type) throws CustomCheckedException;

    NotificationConfig getNotificationConfig(Long branchId, String type) throws CustomCheckedException;

    List<NotificationConfig> getAllNotificationConfig();

    NotificationGeneralConfig getNotificationGeneralConfig(String type) throws CustomCheckedException;

    List<NotificationGeneralConfig> getAllNotificationGeneralConfig();

    NotificationGeneralConfig createNewGeneralConfig(String type) throws CustomCheckedException;

    NotificationGeneralConfig toggleGeneralConfigSwitch(String type, String action) throws CustomCheckedException;

    NotificationConfig toggleConfigSwitch(Long branchId, String type, String action) throws CustomCheckedException;
}
