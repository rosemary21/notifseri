package com.creditville.notifications.services.impl;

import com.creditville.notifications.models.EmailAudit;
import com.creditville.notifications.models.ExcludedEmail;
import com.creditville.notifications.models.FailedEmail;
import com.creditville.notifications.repositories.EmailAuditRepository;
import com.creditville.notifications.repositories.ExcludedEmailRepository;
import com.creditville.notifications.repositories.FailedEmailRepository;
import com.creditville.notifications.services.EmailService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

/**
 * Created by Chuks on 02/07/2021.
 */
@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private FailedEmailRepository failedEmailRepository;

    @Autowired
    private EmailAuditRepository emailAuditRepository;

    @Autowired
    private ExcludedEmailRepository excludedEmailRepository;

    @Override
    public FailedEmail saveFailedEmail(Map<String, String> emailData, String emailSubject, String htmlEmailMessage, String failReason) {
        LocalDate paymentDate = emailData.get("paymentDate") != null ? LocalDate.parse(emailData.get("paymentDate")) : null;
        if(failedEmailRepository.findByToAddressAndSubjectAndPaymentDate(emailData.get("toAddress"), emailSubject, paymentDate) == null)
            return failedEmailRepository.save(new FailedEmail(
                    emailData.get("customerName"),
                    emailData.get("toAddress"),
                    emailSubject,
                    htmlEmailMessage,
                    emailData.get("paymentDate") != null ? LocalDate.parse(emailData.get("paymentDate")) : null,
                    failReason));
        return null;
    }

    @Override
    public FailedEmail saveFailedEmail(ObjectNode emailData, String emailSubject, String htmlEmailMessage, String failReason) {
//        System.out.println("Reason: "+ failReason);
        String mainReason = failReason.contains(":") ? failReason.split(":")[0] : failReason;
        LocalDate paymentDate = emailData.get("paymentDate") != null ? LocalDate.parse(emailData.get("paymentDate").toString()) : null;
        if(failedEmailRepository.findByToAddressAndSubjectAndPaymentDate(emailData.get("toAddress").toString(), emailSubject, paymentDate) == null)
            return failedEmailRepository.save(new FailedEmail(
                    emailData.get("customerName") != null ? emailData.get("customerName").textValue() : null,
                    emailData.get("toAddress").textValue(),
                    emailSubject,
                    htmlEmailMessage,
                    emailData.get("paymentDate") != null ? LocalDate.parse(emailData.get("paymentDate").textValue()) : null,
                    mainReason));
        return null;
    }

    @Override
    public boolean alreadySentOutEmailToday(String toAddress, String toName, String subject, LocalDate paymentDate) {
        EmailAudit emailAudit = emailAuditRepository.findByToAddressAndToNameAndSubjectAndPaymentDate(toAddress, toName, subject, paymentDate);
        return (emailAudit != null);
    }

    @Override
    public EmailAudit auditSuccessfulEmail(Map<String, String> emailData, String emailSubject) {
        return emailAuditRepository.save(new EmailAudit(
                emailData.get("toAddress"),
                emailData.get("customerName"),
                emailSubject,
                emailData.get("paymentDate") != null ? LocalDate.parse(emailData.get("paymentDate")) : null)
        );
    }

    @Override
    public EmailAudit auditSuccessfulEmail(ObjectNode emailData, String emailSubject) {
        return emailAuditRepository.save(new EmailAudit(
                emailData.get("toAddress").toString(),
                emailData.get("customerName") != null ? emailData.get("customerName").toString() : null,
                emailSubject,
                emailData.get("paymentDate") != null ? LocalDate.parse(emailData.get("paymentDate").toString()) : null)
        );
    }

    @Override
    public boolean isEmailExcluded(String emailAddress) {
        ExcludedEmail excludedEmail = excludedEmailRepository.findByEmailAddress(emailAddress);
        return excludedEmail != null;
    }

    @Override
    public void createNewExcludedEmailAddresses(String ...emailAddressesToBeExcluded) {
        if(emailAddressesToBeExcluded != null) {
            for (String emailAddress : emailAddressesToBeExcluded) {
                if(excludedEmailRepository.findByEmailAddress(emailAddress) == null)
                    excludedEmailRepository.save(new ExcludedEmail(emailAddress));
            }
        }
    }

    @Override
    public void removeEmailAddressFromExceptionList(String emailAddress) {
        ExcludedEmail excludedEmail = excludedEmailRepository.findByEmailAddress(emailAddress);
        if(excludedEmail != null) {
            excludedEmailRepository.deleteByExcludedEmailId(excludedEmail.getId());
        }
    }
}
