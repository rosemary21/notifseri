package com.creditville.notifications.services;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.MailMonitor;

import java.util.List;

public interface MailMonitorService {
    MailMonitor getDailyEvent(String operationName) throws CustomCheckedException;

    MailMonitor modifyDailyMonitor(String operationName, Long successCount, Long failureCount) throws CustomCheckedException;

    List<MailMonitor> getAllDailyEventOperations();
}
