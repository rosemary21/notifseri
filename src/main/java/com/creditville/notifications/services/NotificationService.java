package com.creditville.notifications.services;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.ExcludedEmail;
import com.creditville.notifications.models.requests.SendEmailRequest;
import com.creditville.notifications.models.requests.SendOnboardMailRequestDTO;
import com.creditville.notifications.models.requests.SendTransactionMailRequestDTO;
import com.creditville.notifications.redwood.model.EmailRequest;
import com.creditville.notifications.services.impl.NotificationServiceImpl;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

/**
 * Created by Chuks on 02/07/2021.
 */
public interface NotificationService {
    void sendEmailNotification(String subject, Map<String, String> notificationData, String templateLocation) throws CustomCheckedException;

    void sendEmailNotification(SendEmailRequest sendEmailRequest) throws CustomCheckedException;

   void  sendAllCientEmail()  throws CustomCheckedException;
    void sendEmailNotification(EmailRequest sendEmailRequest) throws CustomCheckedException;

    void sendEmailBroadcastNotification(SendEmailRequest sendEmailRequest) throws CustomCheckedException;

    void sendTransactionEmail(SendTransactionMailRequestDTO request) throws CustomCheckedException;

    void sendCompleteRegistrationEmail(SendOnboardMailRequestDTO request) throws CustomCheckedException;

    NotificationServiceImpl.NotificationStatistics getCurrentNotificationStatistics();

    List<ExcludedEmail> getMailExceptionList();

//    void addToExceptionList(AddToExceptionListForm exceptionListForm);

//    void removeFromExceptionList(String emailAddress);
}
