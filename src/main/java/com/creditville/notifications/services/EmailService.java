package com.creditville.notifications.services;

import com.creditville.notifications.models.EmailAudit;
import com.creditville.notifications.models.FailedEmail;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.util.Map;

/**
 * Created by Chuks on 02/09/2021.
 */
public interface EmailService {
    FailedEmail saveFailedEmail(Map<String, String> emailData, String emailSubject, String htmlEmailMessage, String failReason);

    FailedEmail saveFailedEmail(ObjectNode emailData, String emailSubject, String htmlEmailMessage, String failReason);

    boolean alreadySentOutEmailToday(String toAddress, String toName, String subject, LocalDate paymentDate);

    EmailAudit auditSuccessfulEmail(Map<String, String> emailData, String emailSubject);

    EmailAudit auditSuccessfulEmail(ObjectNode emailData, String emailSubject);

    boolean isEmailExcluded(String emailAddress);

    void createNewExcludedEmailAddresses(String ...emailAddressesToBeExcluded);

    void removeEmailAddressFromExceptionList(String emailAddress);

    boolean emailAlreadyFailed(LocalDate paymentDate, String toAddress, String emailSubject);
}
