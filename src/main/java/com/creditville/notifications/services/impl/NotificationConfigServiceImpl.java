package com.creditville.notifications.services.impl;

/* Created by David on 6/10/2021 */

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.Branch;
import com.creditville.notifications.models.NotificationConfig;
import com.creditville.notifications.models.NotificationGeneralConfig;
import com.creditville.notifications.models.NotificationType;
import com.creditville.notifications.repositories.NotificationConfigRepository;
import com.creditville.notifications.repositories.NotificationGeneralConfigRepository;
import com.creditville.notifications.services.BranchService;
import com.creditville.notifications.services.NotificationConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationConfigServiceImpl implements NotificationConfigService {
    @Autowired
    private NotificationConfigRepository notificationConfigRepository;

    @Autowired
    private BranchService branchService;

    @Autowired
    private NotificationGeneralConfigRepository notificationGeneralConfigRepository;

    @Override
    public NotificationConfig createNew(Long branchId, String type) throws CustomCheckedException {
        NotificationType notificationType = this.getNotificationType(type);
        Branch branch = branchService.getBranch(branchId);
        if(notificationConfigRepository.findByNotificationTypeAndBranch(notificationType, branch) == null) {
            return notificationConfigRepository.save(new NotificationConfig(notificationType, branch));
        }
        throw new CustomCheckedException(String.format("A notification configuration already exists for branch with id: %d and type: %s", branchId, type));
    }

    private NotificationType getNotificationType(String type) throws CustomCheckedException {
        NotificationType notificationType1;
        try{
            notificationType1 = NotificationType.valueOf(type.toUpperCase());
        }catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            throw new CustomCheckedException(String.format("Invalid notification type provided: %s", type));
        }
        return notificationType1;
    }

    @Override
    public NotificationConfig getNotificationConfig(Long branchId, String type) throws CustomCheckedException {
        Branch branch = branchService.getBranch(branchId);
        NotificationType notificationType = this.getNotificationType(type);
        return notificationConfigRepository.findByNotificationTypeAndBranch(notificationType, branch);
    }

    @Override
    public List<NotificationConfig> getAllNotificationConfig() {
        return notificationConfigRepository.findAll();
    }

    @Override
    public NotificationGeneralConfig getNotificationGeneralConfig(String type) throws CustomCheckedException {
        NotificationType notificationType = this.getNotificationType(type);

        return notificationGeneralConfigRepository.findByNotificationType(notificationType);
    }

    @Override
    public List<NotificationGeneralConfig> getAllNotificationGeneralConfig() {
        return notificationGeneralConfigRepository.findAll();
    }

    @Override
    public NotificationGeneralConfig createNewGeneralConfig(String type) throws CustomCheckedException {
        NotificationType notificationType = this.getNotificationType(type);
        if(notificationGeneralConfigRepository.findByNotificationType(notificationType) == null) {
            return notificationGeneralConfigRepository.save(new NotificationGeneralConfig(notificationType));
        }
        throw new CustomCheckedException(String.format("A notification configuration already exists for type: %s", type));
    }

    @Override
    public NotificationGeneralConfig toggleGeneralConfigSwitch(String type, String action) throws CustomCheckedException {
        NotificationGeneralConfig notificationConfig = this.getNotificationGeneralConfig(type);
        switch (action.toUpperCase()) {
            case "ON":
                notificationConfig.setIsEnabled(true);
                break;
            case "OFF":
                notificationConfig.setIsEnabled(false);
                break;
            default:
                throw new CustomCheckedException("Invalid toggle action provided: "+ action);
        }
        notificationGeneralConfigRepository.save(notificationConfig);
        return this.getNotificationGeneralConfig(type);
    }

    @Override
    public NotificationConfig toggleConfigSwitch(Long branchId, String type, String action) throws CustomCheckedException {
        NotificationConfig notificationConfig = this.getNotificationConfig(branchId, type);
        switch (action.toUpperCase()) {
            case "ON":
                notificationConfig.setIsEnabled(true);
                break;
            case "OFF":
                notificationConfig.setIsEnabled(false);
                break;
            default:
                throw new CustomCheckedException("Invalid toggle action provided: "+ action);
        }
        notificationConfigRepository.save(notificationConfig);
        return this.getNotificationConfig(branchId, type);
    }
}
