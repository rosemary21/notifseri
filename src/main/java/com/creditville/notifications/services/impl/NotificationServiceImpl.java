package com.creditville.notifications.services.impl;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.ExcludedEmail;
import com.creditville.notifications.models.requests.SendEmailRequest;
import com.creditville.notifications.redwood.model.EmailRequest;
import com.creditville.notifications.repositories.EmailAuditRepository;
import com.creditville.notifications.repositories.ExcludedEmailRepository;
import com.creditville.notifications.repositories.FailedEmailRepository;
import com.creditville.notifications.services.EmailService;
import com.creditville.notifications.services.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.EmailPopulatingBuilder;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.activation.FileDataSource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Chuks on 02/07/2021.
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    @Qualifier("templateEngine")
    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private Mailer mailer;

    @Value("${mail.smt.sendAsync}")
    private Boolean async;

    @Value("${mail.smt.sender}")
    private String senderName;

    @Value("${mail.smt.senderEmail}")
    private String senderEmail;

    @Value("${app.notificationsEnabled}")
    private Boolean notificationsEnabled;

    @Autowired
    private EmailService emailService;

    @Autowired
    private FailedEmailRepository failedEmailRepository;

    @Autowired
    private EmailAuditRepository emailAuditRepository;

    @Autowired
    private ExcludedEmailRepository excludedEmailRepository;

/**
 * @param subject is the mail subject and is a required parameter
 * @param templateLocation is the location of the HTML email template and is a required parameter
 * @param  notificationData must contain toName and toAddress as member data
 *
 * @throws CustomCheckedException when an error occurs
 *
 * @implNote if toAddress exists in excluded_email table, mail won't be sent out
 *
 * This method sends out notifications with the SMTP properties configured in the application.properties file
 * **/
    @Override
    public void sendEmailNotification(String subject, Map<String, String> notificationData, String templateLocation) throws CustomCheckedException {
        Context context = new Context();
        if(notificationData == null) throw new CustomCheckedException("Notification data cannot be null");
        if(!notificationData.containsKey("toName") && !notificationData.containsKey("toAddress"))
            throw new CustomCheckedException("The parameters toName and toAddress are required to send an email notification");
        if(templateLocation == null) throw new CustomCheckedException("Template location is required");
        if(templateLocation.trim().equals("")) throw new CustomCheckedException("Template location cannot be empty");
        if(subject == null) throw new CustomCheckedException("Mail subject is required");
        if(subject.trim().equals("")) throw new CustomCheckedException("Mail subject cannot be empty");
        for(Map.Entry<String, String> entryItem : notificationData.entrySet())
            context.setVariable(entryItem.getKey(), entryItem.getValue());
        String content  = templateEngine.process(templateLocation, context);
        Email email = EmailBuilder.startingBlank()
                .from(senderName, senderEmail)
                .to(notificationData.get("toName"), notificationData.get("toAddress"))
                .withSubject(subject)
                .withHTMLText(content).buildEmail();
//        System.out.println("Email: "+ email.getHTMLText());
        if(notificationsEnabled) {
            try {
                if(!emailService.isEmailExcluded(notificationData.get("toAddress"))) {
                    mailer.sendMail(email, async);
                    emailService.auditSuccessfulEmail(notificationData, subject);
                }
            }catch (Exception ex) {
                emailService.saveFailedEmail(notificationData, subject, email.getHTMLText(), ex.getMessage());
                log.info("Email sending failed: "+ ex.getMessage());
                throw new CustomCheckedException("Email sending failed");
            }
        }
        else throw new CustomCheckedException("Oops! Unable to dispatch notifications at the moment as it is disabled from config");
    }

    @Override
    public void sendEmailNotification(SendEmailRequest sendEmailRequest) throws CustomCheckedException {
        Context context = new Context();
        ObjectNode mailData = sendEmailRequest.getMailData();
        if(mailData.isEmpty())
            throw new CustomCheckedException("Mail data cannot be empty");
        if(sendEmailRequest.getMailTemplate().trim().equals("")) throw new CustomCheckedException("Mail template cannot be empty");
        if(!mailData.has("toAddress"))
            throw new CustomCheckedException("To address is required to send an email notification");
        if(sendEmailRequest.getMailTemplate().trim().equals("")) throw new CustomCheckedException("Mail template cannot be empty");
        if(sendEmailRequest.getMailSubject().trim().equals("")) throw new CustomCheckedException("Mail subject cannot be empty");
        Iterator<Map.Entry<String, JsonNode>> jsonNodeIterator = mailData.fields();
        while(jsonNodeIterator.hasNext()) {
            Map.Entry<String, JsonNode> jsonNode = jsonNodeIterator.next();
            log.info("getting the key node {}",jsonNode.getKey());
            context.setVariable(jsonNode.getKey(), jsonNode.getValue().textValue());
        }
        String mailTemplate = sendEmailRequest.getMailTemplate();
        if(mailTemplate.equals("custom")) {
            context.setVariable("customMailSubject", sendEmailRequest.getMailSubject());
            context.setVariable("customCustomerName", sendEmailRequest.getMailData().get("toName").textValue());
        }
        if(mailTemplate.equals("disburseemail")){
            context.setVariable("message", sendEmailRequest.getMailData().get("message").textValue());
            context.setVariable("loanId", sendEmailRequest.getMailData().get("loanId").textValue());
            context.setVariable("emailTo",sendEmailRequest.getMailData().get("emailTo").textValue());
        }
        String templateLocation = this.getTemplateLocation(sendEmailRequest.getMailTemplate());
        String content = templateEngine.process(templateLocation, context);
        if(mailData.get("toAddress") == null) throw new CustomCheckedException("To address cannot be null");
        String toAddresses = mailData.get("toAddress").textValue();
        List<String> toAddressList = new ArrayList<>();
        if(toAddresses.contains(",")) {
            String[] parts = toAddresses.split(",");
            toAddressList = Arrays.stream(parts).collect(Collectors.toList());
        }else toAddressList.add(toAddresses);

        JsonNode ccAddress = mailData.get("ccAddress");
        String ccAddresses = ccAddress != null ? ccAddress.textValue() : null;
        System.out.println("getting the ccAddress"+ccAddresses);
        List<String> ccAddressList = new ArrayList<>();
        if(ccAddress != null) {
            if (ccAddresses.contains(",")) {
                String[] parts = ccAddresses.split(",");
                ccAddressList = Arrays.stream(parts).collect(Collectors.toList());
            } else ccAddressList.add(ccAddresses);
        }

        JsonNode bccAddress = mailData.get("bccAddress");
        String bccAddresses = bccAddress != null ? bccAddress.textValue() : null;
        List<String> bccAddressList = new ArrayList<>();
        if(bccAddress != null) {
            if (bccAddresses.contains(",")) {
                String[] parts = bccAddresses.split(",");
                bccAddressList = Arrays.stream(parts).collect(Collectors.toList());
            } else bccAddressList.add(bccAddresses);
        }

        System.out.println("getting the toAddressList {}"+toAddressList);
        System.out.println("getting the ccAddressList"+ccAddressList);

        EmailPopulatingBuilder emailPopulatingBuilder = EmailBuilder.startingBlank()
                .from(senderName, senderEmail)
                .to(null, toAddressList)
                .cc(null, ccAddressList)
                .bcc(null, bccAddressList)
                .withSubject(sendEmailRequest.getMailSubject())
                .withHTMLText(content);
        Email email = mailTemplate.equals("investmentCertificate") ?
                emailPopulatingBuilder
                        .withAttachment("investment-certificate.pdf", new FileDataSource(sendEmailRequest.getMailData().get("attachmentLocation").textValue()))
                        .buildEmail() :
                emailPopulatingBuilder
                        .buildEmail();

        if(notificationsEnabled) {
            try {
                if(!emailService.isEmailExcluded(mailData.get("toAddress").textValue())) {
                    mailer.sendMail(email, async);
                    emailService.auditSuccessfulEmail(mailData, sendEmailRequest.getMailSubject());
                }
            }catch (Exception ex) {
                ex.printStackTrace();
                emailService.saveFailedEmail(mailData, sendEmailRequest.getMailSubject(), email.getHTMLText(), ex.getMessage());
                log.info("Email sending failed: "+ ex.getMessage());
                throw new CustomCheckedException("Email sending failed");
            }
        }
        else throw new CustomCheckedException("Oops! Unable to dispatch notifications at the moment as it is disabled from config");
    }


    @Override
    public void sendEmailNotification(EmailRequest sendEmailRequest) throws CustomCheckedException {
        Context context = new Context();
        context.setVariable("mailSubject", "Contact Redwood");
        context.setVariable("customMessage", sendEmailRequest.getMessage());
        context.setVariable("customerEmail",sendEmailRequest.getEmail());
        context.setVariable("customerAddress",sendEmailRequest.getAddress());
        context.setVariable("customerName",sendEmailRequest.getName());
        String toAddresses ="chioma.chukelu@creditville.ng";
        List<String> toAddressList = new ArrayList<>();
        if(toAddresses.contains(",")) {
            String[] parts = toAddresses.split(",");
            toAddressList = Arrays.stream(parts).collect(Collectors.toList());
        }else toAddressList.add(toAddresses);
        for(String customer :  toAddressList) {
            String templateLocation = this.getTemplateLocation("redwood");
            String content = templateEngine.process(templateLocation, context);
            Email email = EmailBuilder.startingBlank()
                    .from(senderName, senderEmail)
                    .to(null, toAddressList)
                    .withSubject("Contact RedWood")
                    .withHTMLText(content).buildEmail();
            if(notificationsEnabled) {
                try {
                    if(!emailService.isEmailExcluded(sendEmailRequest.getAddress())) {
                        mailer.sendMail(email, async);
                      //  emailService.auditSuccessfulEmail(mailData, sendEmailRequest.getMailSubject());
                    }
                }catch (Exception ex) {
                  //  emailService.saveFailedEmail(mailData, sendEmailRequest.getMailSubject(), email.getHTMLText(), ex.getMessage());
                    log.info("Email sending failed: "+ ex.getMessage());
                    throw new CustomCheckedException("Email sending failed");
                }
            }
            else throw new CustomCheckedException("Oops! Unable to dispatch notifications at the moment as it is disabled from config");
        }
    }

    @Override
    public void sendEmailBroadcastNotification(SendEmailRequest sendEmailRequest) throws CustomCheckedException {
        Context context = new Context();
        ObjectNode mailData = sendEmailRequest.getMailData();
        if(mailData.isEmpty())
            throw new CustomCheckedException("Mail data cannot be empty");
        if(sendEmailRequest.getMailTemplate().trim().equals("")) throw new CustomCheckedException("Mail template cannot be empty");
        if(sendEmailRequest.getMailSubject().trim().equals("")) throw new CustomCheckedException("Mail subject cannot be empty");
        context.setVariable("mailSubject", sendEmailRequest.getMailSubject());
        context.setVariable("customMessage", sendEmailRequest.getMailMessage());
        Iterator<Map.Entry<String, JsonNode>> jsonNodeIterator = mailData.fields();
        while(jsonNodeIterator.hasNext()) {
            Map.Entry<String, JsonNode> jsonNode = jsonNodeIterator.next();
            context.setVariable(jsonNode.getKey(), jsonNode.getValue().textValue());
        }
        if(mailData.get("toAddress") == null) throw new CustomCheckedException("To address cannot be null");
        String toAddresses = mailData.get("toAddress").textValue();
        List<String> toAddressList = new ArrayList<>();
        if(toAddresses.contains(",")) {
            String[] parts = toAddresses.split(",");
            toAddressList = Arrays.stream(parts).collect(Collectors.toList());
        }else toAddressList.add(toAddresses);
        for(String customer :  toAddressList) {
            String templateLocation = this.getTemplateLocation(sendEmailRequest.getMailTemplate());
            String content = templateEngine.process(templateLocation, context);
            Email email = EmailBuilder.startingBlank()
                    .from(senderName, senderEmail)
                    .to(null, customer)
                    .withSubject(sendEmailRequest.getMailSubject())
                    .withHTMLText(content).buildEmail();
            if(notificationsEnabled) {
                try {
                    if(!emailService.isEmailExcluded(mailData.get("toAddress").textValue())) {
                        mailer.sendMail(email, async);
                        emailService.auditSuccessfulEmail(mailData, sendEmailRequest.getMailSubject());
                    }
                }catch (Exception ex) {
                    emailService.saveFailedEmail(mailData, sendEmailRequest.getMailSubject(), email.getHTMLText(), ex.getMessage());
                    log.info("Email sending failed: "+ ex.getMessage());
                    throw new CustomCheckedException("Email sending failed");
                }
            }
            else throw new CustomCheckedException("Oops! Unable to dispatch notifications at the moment as it is disabled from config");
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public class NotificationStatistics {
        private Long failedCount;
        private Long successfulCount;
    }

    @Override
    public NotificationStatistics getCurrentNotificationStatistics() {
        return new NotificationStatistics(failedEmailRepository.count(), emailAuditRepository.count());
    }

    @Override
    public List<ExcludedEmail> getMailExceptionList() {
        return excludedEmailRepository.findAll();
    }

    private String getTemplateLocation(String templateName) throws CustomCheckedException {
        System.out.println("getting the template name {}"+templateName);
        switch (templateName) {
            case "cardTokenization":
                return "email/card-tokenization";
            case "cardTokenized":
                return "email/card-successfully-tokenized";
            case "cardNotTokenized":
                return "email/card-not-tokenized";
            case "repaymentFailure":
                return "email/repayment-failure";
            case "repaymentSuccess":
                return "email/repayment-success";
            case "dispatchedMails":
                return "email/dispatched-mails";
            case "passwordReset":
                return "email/password-reset";
            case "userCreated":
                return "email/user-created";
            case "dueRental":
                return "email/due_rental";
            case "arrears":
                return "email/arrears";
            case "chequeLodgement":
                return "email/cheque_lodgement";
            case "postMaturity":
                return "email/post_maturity";
            case "investmentCertificate":
                return "email/investment-certificate";
            case "custom":
                return "email/custom-message";
            case "activateOtp":
                return "email/activate-otp";
            case "sendForm":
                return "email/send-form";
            case "disburseemail":
                return "email/disburse";
            case "terminateMandate":
                return "email/terminateMandate";
            case "redwood":
                return "email/redwood";
            case "failedtranfer":
                return "email/failedtransfer";
            case "reversetranfer":
                return "email/reversetranfer";

            case "disbursefailed":
                return "email/disbursefailed";

            case "mandateActivated":
                return "email/mandateActivated";
            case "broadcastredwood":
                return "email/redwoodbroadcast";


            default:
                throw new CustomCheckedException("Invalid template name provided");
        }
    }

//    @Override
//    public void addToExceptionList(AddToExceptionListForm exceptionListForm) {
//        emailService.createNewExcludedEmailAddresses(exceptionListForm.getEmailAddress());
//    }
//
//    @Override
//    public void removeFromExceptionList(String emailAddress) {
//        emailService.removeEmailAddressFromExceptionList(emailAddress);
//    }
}
