package com.creditville.notifications.services.impl;

//import com.creditville.notifications.configurations.MailConfiguration;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.EmailTemplate;
import com.creditville.notifications.models.ExcludedEmail;
import com.creditville.notifications.models.SmsTemplate;
import com.creditville.notifications.models.To;
import com.creditville.notifications.models.requests.SendEmailRequest;
import com.creditville.notifications.models.response.Client;
import com.creditville.notifications.redwood.model.EmailRequest;
import com.creditville.notifications.repositories.*;
import com.creditville.notifications.services.ClientService;
import com.creditville.notifications.services.EmailService;
import com.creditville.notifications.services.NotificationService;
import com.creditville.notifications.sms.dto.RequestDTO;
import com.creditville.notifications.sms.dto.SMSDTO;
import com.creditville.notifications.sms.services.SmsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.IOUtils;
import org.json.simple.JSONArray;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.EmailPopulatingBuilder;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.api.mailer.AsyncResponse;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.util.ByteArrayDataSource;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    SmsService smsService;


    @Value("${app.sms.source}")
    private String smssource;
//    @Autowired
//    private Mailer mailer;

    @Value("${mail.smt.sendAsync}")
    private Boolean async;

//    @Autowired
//    MailConfiguration mailConfiguration;

    @Value("${mail.smt.sender}")
    private String senderName;

    @Value("${redwood.mail.smt.sender}")
    private String redWoodSenderName;

    @Value("${mail.smt.senderEmail}")
    private String senderEmail;

    @Value("${redwood.mail.smt.senderEmail}")
    private String redWoodSenderEmail;

    @Value("${app.notificationsEnabled}")
    private Boolean notificationsEnabled;

    @Autowired
    private EmailService emailService;

    @Value("${redwood.mail.smt.user}")
    String redwoodmailUser;
    @Value("${redwood.mail.smt.pass}")
    String redwoodmailPass;
    @Value("${redwood.mail.smt.url}")
    String redwoodmailUrl;
    @Value("${redwood.mail.smt.port}")
    Integer redwoodmailPort;

    @Value("${mail.smt.user}")
    String mailUser;
    @Value("${mail.smt.pass}")
    String mailPass;
    @Value("${mail.smt.url}")
    String mailUrl;
    @Value("${mail.smt.port}")
    Integer mailPort;

    @Autowired
    private FailedEmailRepository failedEmailRepository;

    @Autowired
    private EmailAuditRepository emailAuditRepository;

    @Autowired
    private ExcludedEmailRepository excludedEmailRepository;

    @Autowired
    BroadCastRepository broadCastRepository;


    @Autowired
    BroadCastSmsRepository broadCastSmsRepository;



    @Autowired
    ClientService clientService;

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

                    Mailer mailer = MailerBuilder.withSMTPServerHost(mailUrl)
                            .withSMTPServerPort(mailPort)
                            .withSMTPServerUsername(mailUser)
                            .withSMTPServerPassword(mailPass)
                            .withTransportStrategy(TransportStrategy.SMTP_TLS).buildMailer();
                             mailer.sendMail(email, async);
                    //mailer.sendMail(email, async);

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
            log.info("getting the text value {}",jsonNode.getValue().textValue());
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
        if(sendEmailRequest.getMailTemplate().equalsIgnoreCase("broadcastredwood")){
            context.setVariable("emailBody",sendEmailRequest.getMailData().get("emailBody").textValue());
        }
        String templateLocation = this.getTemplateLocation(sendEmailRequest.getMailTemplate());
        String content = templateEngine.process(templateLocation, context);
        if(mailData.get("toAddress") == null) throw new CustomCheckedException("To address cannot be null");
        String toAddresses = mailData.get("toAddress").textValue();
        log.info("getting the address to send email <><> {}",toAddresses);
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
        String senderNameValue=senderName;
        String SenderEmailValue=senderEmail;
        if(sendEmailRequest.getMailTemplate().equalsIgnoreCase("broadcastredwood")){
            log.info("getting the broadcast email information");
             senderNameValue=redWoodSenderName;
             SenderEmailValue=redWoodSenderEmail;
        }
            EmailPopulatingBuilder emailPopulatingBuilder = EmailBuilder.startingBlank()
                .from(senderNameValue, SenderEmailValue)
                .to(null, toAddressList)
                .cc(null, ccAddressList)
                .bcc(null, bccAddressList)
                .withSubject(sendEmailRequest.getMailSubject())
                .withHTMLText(content);

        Email email =null;
        if(mailTemplate.equals("investmentCertificate")){
             email = emailPopulatingBuilder
                    .withAttachment("investment-certificate.pdf", new FileDataSource(sendEmailRequest.getMailData().get("attachmentLocation").textValue()))
                    .buildEmail() ;
        }else if(mailTemplate.equals("accountStatement")){
            byte[] decoded = java.util.Base64.getDecoder().decode(sendEmailRequest.getMailData().get("file").textValue());

            email = emailPopulatingBuilder
                    .withAttachment("accountStatement.pdf",new ByteArrayDataSource(decoded,"application/pdf"))
                    .buildEmail();

        }else{
            email = emailPopulatingBuilder
                    .buildEmail();
        }
//        Email email = mailTemplate.equals("investmentCertificate") ?
//                emailPopulatingBuilder
//                        .withAttachment("investment-certificate.pdf", new FileDataSource(sendEmailRequest.getMailData().get("attachmentLocation").textValue()))
//                        .buildEmail() :
//                emailPopulatingBuilder
//                        .buildEmail();

        if(notificationsEnabled) {
            try {
                 if(!(sendEmailRequest.getMailTemplate().equalsIgnoreCase("broadcastredwood"))) {
                     if (!emailService.isEmailExcluded(mailData.get("toAddress").textValue())) {
                         log.info("Getting the creditville information details {} ",email);

                         Mailer mailer = MailerBuilder.withSMTPServerHost(mailUrl)
                                 .withSMTPServerPort(mailPort)
                                 .withSMTPServerUsername(mailUser)
                                 .withSMTPServerPassword(mailPass)
                                 .withTransportStrategy(TransportStrategy.SMTP_TLS).buildMailer();
                         mailer.sendMail(email, async);
                         log.info("THE EMAIL BROADCAST HAS BEEN SUCCESSFULLY SENT TO CUSTOMER"+toAddresses);


                         emailService.auditSuccessfulEmail(mailData, sendEmailRequest.getMailSubject());
                     }
                 }
            }catch (Exception ex) {
                ex.printStackTrace();
                emailService.saveFailedEmail(mailData, sendEmailRequest.getMailSubject(), email.getHTMLText(), ex.getMessage());
                log.info("Email sending failed: "+ ex.getMessage());
                throw new CustomCheckedException("Email sending failed");
            }
            if(sendEmailRequest.getMailTemplate().equalsIgnoreCase("broadcastredwood")){
                log.info("Getting the redwood information details");
                Mailer mailer = MailerBuilder.withSMTPServerHost(redwoodmailUrl)
                        .withSMTPServerPort(redwoodmailPort)
                        .withSMTPServerUsername(redwoodmailUser)
                        .withSMTPServerPassword(redwoodmailPass)
                        .withTransportStrategy(TransportStrategy.SMTP_TLS).buildMailer();
                mailer.sendMail(email, async);

                log.info("THE EMAIL BROADCAST HAS BEEN SUCCESSFULLY SENT TO CUSTOMER"+toAddresses);

            }


        }
        else throw new CustomCheckedException("Oops! Unable to dispatch notifications at the moment as it is disabled from config");
    }

    @Override
    public void sendAllCientEmail() throws CustomCheckedException {
        Context context = new Context();
        EmailTemplate emailTemplate= broadCastRepository.findBySender("CreditVille");
        SmsTemplate smsTemplate=broadCastSmsRepository.findBySender("Creditville");

        EmailTemplate redwoodTemplate= broadCastRepository.findBySender("RedWood");

        List<String> arrayList=new ArrayList<>();
        context.setVariable("emailBody",emailTemplate.getTemplateMessage());
        String templateLocation = this.getTemplateLocation("broadcastredwood");
        String content = templateEngine.process(templateLocation, context);


        if(emailTemplate.getEnableBroadcast().equalsIgnoreCase("Y")){
            List<Client> clients= clientService.fetchClients();

            log.info("getting the braodcast {}");
            String emailAddress="";
            // for(Client client:clients){
            //   emailAddress=client.getEmail();
            log.info("getting the email address <><>< {}",emailAddress);
            //  String toAddresses = emailAddress;
            List<String> toAddressList = new ArrayList<>();
            toAddressList.add("omotayo.owolabi@creditville.ng");
            toAddressList.add("chioma.chukelu@creditville.ng");

//                if(toAddresses.contains(",")) {
//                    String[] parts = toAddresses.split(",");
//                    toAddressList = Arrays.stream(parts).collect(Collectors.toList());
//                }else toAddressList.add(toAddresses);
            String   senderNameValue=senderName;
            String  SenderEmailValue=senderEmail;
            EmailPopulatingBuilder emailPopulatingBuilder = EmailBuilder.startingBlank()
                    .from(senderNameValue, SenderEmailValue)
                    .to(null, toAddressList)
                    .withSubject(emailTemplate.getEmailSubject())
                    .withHTMLText(content);
            Email email =  emailPopulatingBuilder
                    .buildEmail();

            if(notificationsEnabled) {
                try{
                    log.info("Getting the redwood information details");
                    Mailer mailer = MailerBuilder.withSMTPServerHost(mailUrl)
                            .withSMTPServerPort(mailPort)
                            .withSMTPServerUsername(mailUser)
                            .withSMTPServerPassword(mailPass)
                            .withTransportStrategy(TransportStrategy.SMTP_TLS).buildMailer();
                    mailer.sendMail(email, async);

                    log.info("THE EMAIL BROADCAST HAS BEEN SUCCESSFULLY SENT TO CUSTOMER");

                }
                catch (Exception e){
                    e.printStackTrace();
                    arrayList.add(emailAddress);

                }
            }

            //    }
            String jsonStr = JSONArray.toJSONString(arrayList);
            EmailTemplate emailTemplate1=broadCastRepository.findBySender("CreditVille");
            emailTemplate1.setFailedEmail(jsonStr);
            emailTemplate1.setEnableBroadcast("N");
            broadCastRepository.save(emailTemplate1);
        }

        if(emailTemplate.getEnableUnregistered().equalsIgnoreCase("Y")){

            try{
                URL u = new URL(emailTemplate.getUnregisteredTemplate());
                InputStream targetStream =u.openStream();
                byte[] bytes = IOUtils.toByteArray(targetStream);
                String contents = new String(bytes, StandardCharsets.UTF_8);
                String[] recipients = contents.split(System.lineSeparator());
                for (String recipient : recipients) {
                    log.info("getting the recipient {}",recipient);
                    String toAddresses = recipient;
                    List<String> toAddressList = new ArrayList<>();
                    if(toAddresses.contains(",")) {
                        String[] parts = toAddresses.split(",");
                        toAddressList = Arrays.stream(parts).collect(Collectors.toList());
                    }else toAddressList.add(toAddresses);

                    String   senderNameValue=senderName;
                    String  SenderEmailValue=senderEmail;
                    EmailPopulatingBuilder emailPopulatingBuilder = EmailBuilder.startingBlank()
                            .from(senderNameValue, SenderEmailValue)
                            .to(null, toAddressList)
                            .withSubject(emailTemplate.getEmailSubject())
                            .withHTMLText(content);
                    Email email =  emailPopulatingBuilder
                            .buildEmail();

                    if(notificationsEnabled) {
                        try{
                            log.info("Getting the redwood information details");
                            Mailer mailer = MailerBuilder.withSMTPServerHost(mailUrl)
                                    .withSMTPServerPort(mailPort)
                                    .withSMTPServerUsername(mailUser)
                                    .withSMTPServerPassword(mailPass)
                                    .withTransportStrategy(TransportStrategy.SMTP_TLS).buildMailer();
                            mailer.sendMail(email, async);

                            log.info("THE EMAIL BROADCAST HAS BEEN SUCCESSFULLY SENT TO CUSTOMER");

                        }
                        catch (Exception e){
                            e.printStackTrace();
                            arrayList.add(toAddresses);

                        }
                    }
                }

                String jsonStr = JSONArray.toJSONString(arrayList);
                EmailTemplate emailTemplate1=broadCastRepository.findBySender("CreditVille");
                emailTemplate1.setFailedEmail(jsonStr);
                emailTemplate1.setEnableUnregistered("N");
                broadCastRepository.save(emailTemplate1);

            }catch (Exception e){
                e.printStackTrace();
            }
        }


        if(smsTemplate.getEnableUnregistered().equalsIgnoreCase("Y")){

            try{
                URL u = new URL(smsTemplate.getUnregisteredTemplate());
                InputStream targetStream =u.openStream();
                byte[] bytes = IOUtils.toByteArray(targetStream);
                String contents = new String(bytes, StandardCharsets.UTF_8);
                String[] recipients = contents.split(System.lineSeparator());
                Map<String ,Integer> keyValue=new HashMap<>();

                for (String recipient : recipients) {
                     String values[]= recipient.split(",");
                    for(int i=2;i<values.length;i++){
                        if(values[1].equalsIgnoreCase("message")){
                            keyValue.put(values[i],i);
                        }
                    }

                    String toAddresses = values[0];
                    if(!(values[1].contains("{")) && !(values[1].equalsIgnoreCase("message"))){
                        log.info("no message format");
                        SMSDTO smsdto=new SMSDTO();
                        RequestDTO requestDTO=new RequestDTO();
                        requestDTO.setText(values[1]);
                        requestDTO.setDest(toAddresses);
                        requestDTO.setSrc(smssource);
                        smsdto.setSms(requestDTO);
                        smsService.sendSingleSms(smsdto);
                    }
                    if(values[1].contains("{") && !(values[1].equalsIgnoreCase("message"))){
                        log.info("message format exist");
                        List<String> result = new ArrayList<>();
                        String rePattern = "\\{(.*?)}";
                        Pattern p = Pattern.compile(rePattern);
                        Matcher m = p.matcher(values[1]);
                        int i=1;
                        StringBuilder messase= new StringBuilder("") ;
                        String formattedString=values[1];


                        while (m.find()) {
                            log.info("getting the result {}",m.group(i));
                            Integer value=keyValue.get(m.group(i));
                            log.info("getting the index value {}",value);
                            log.info("value to be replaced {}",values[value]);
                             messase= new StringBuilder(formattedString.replace("{"+m.group(i)+"}",values[value]));
                            formattedString=messase.toString();
                             log.info("getting the message value {}",formattedString);
                           // result.add(m.group(i));

                        }

                        log.info("final value {}",messase);
                        SMSDTO smsdto=new SMSDTO();
                        RequestDTO requestDTO=new RequestDTO();
                        requestDTO.setText(formattedString);
                        requestDTO.setDest(toAddresses);
                        requestDTO.setSrc(smssource);
                        smsdto.setSms(requestDTO);
                        smsService.sendSingleSms(smsdto);

                    }


                }
                catch (Exception e){
                    e.printStackTrace();
                    arrayList.add(emailAddress);

                String jsonStr = JSONArray.toJSONString(arrayList);
                SmsTemplate emailTemplate1=broadCastSmsRepository.findBySender("CreditVille");
                emailTemplate1.setFailedEmail(jsonStr);
                emailTemplate1.setEnableUnregistered("N");
                broadCastSmsRepository.save(emailTemplate1);

            }catch (Exception e){
                e.printStackTrace();
            }
        }


        if(smsTemplate.getEnableBroadcast().equalsIgnoreCase("Y")){
            List<Client> clients= clientService.fetchClients();

            log.info("getting the braodcast {}");
            String emailAddress="";
            // for(Client client:clients){
            //   emailAddress=client.getEmail();
            log.info("getting the email address <><>< {}",emailAddress);
            //  String toAddresses = emailAddress;
            List<String> toAddressList = new ArrayList<>();
            toAddressList.add("2348169696443");
            toAddressList.add("2348169696443");
//                if(toAddresses.contains(",")) {
//                    String[] parts = toAddresses.split(",");
//                    toAddressList = Arrays.stream(parts).collect(Collectors.toList());
//                }else toAddressList.add(toAddresses);


            //    }

            SMSDTO smsdto=new SMSDTO();
            RequestDTO requestDTO=new RequestDTO();
            requestDTO.setText(smsTemplate.getTemplateMessage());
            requestDTO.setDest("2348169696443");
            requestDTO.setSrc(smssource);
            smsdto.setSms(requestDTO);
            smsService.sendSingleSms(smsdto);
            String jsonStr = JSONArray.toJSONString(arrayList);
            SmsTemplate emailTemplate1=broadCastSmsRepository.findBySender("CreditVille");

            emailTemplate1.setFailedEmail(jsonStr);
            emailTemplate1.setEnableBroadcast("N");
            broadCastSmsRepository.save(emailTemplate1);
        }

        if(emailTemplate.getEnableUnregistered().equalsIgnoreCase("Y")){

            try{
                URL u = new URL(emailTemplate.getUnregisteredTemplate());
                InputStream targetStream =u.openStream();
                byte[] bytes = IOUtils.toByteArray(targetStream);
                String contents = new String(bytes, StandardCharsets.UTF_8);
                String[] recipients = contents.split(System.lineSeparator());
                for (String recipient : recipients) {
                    log.info("getting the recipient {}",recipient);
                    String toAddresses = recipient;
                    List<String> toAddressList = new ArrayList<>();
                    if(toAddresses.contains(",")) {
                        String[] parts = toAddresses.split(",");
                        toAddressList = Arrays.stream(parts).collect(Collectors.toList());
                    }else toAddressList.add(toAddresses);

                    String   senderNameValue=senderName;
                    String  SenderEmailValue=senderEmail;
                    EmailPopulatingBuilder emailPopulatingBuilder = EmailBuilder.startingBlank()
                            .from(senderNameValue, SenderEmailValue)
                            .to(null, toAddressList)
                            .withSubject(emailTemplate.getEmailSubject())
                            .withHTMLText(content);
                    Email email =  emailPopulatingBuilder
                            .buildEmail();

                    if(notificationsEnabled) {
                        try{
                            log.info("Getting the redwood information details");
                            Mailer mailer = MailerBuilder.withSMTPServerHost(mailUrl)
                                    .withSMTPServerPort(mailPort)
                                    .withSMTPServerUsername(mailUser)
                                    .withSMTPServerPassword(mailPass)
                                    .withTransportStrategy(TransportStrategy.SMTP_TLS).buildMailer();
                            mailer.sendMail(email, async);

                            log.info("THE EMAIL BROADCAST HAS BEEN SUCCESSFULLY SENT TO CUSTOMER");

                        }
                        catch (Exception e){
                            e.printStackTrace();
                            arrayList.add(toAddresses);

                        }
                    }
                }

                String jsonStr = JSONArray.toJSONString(arrayList);
                EmailTemplate emailTemplate1=broadCastRepository.findBySender("CreditVille");
                emailTemplate1.setFailedEmail(jsonStr);
                emailTemplate1.setEnableUnregistered("N");
                broadCastRepository.save(emailTemplate1);

            }catch (Exception e){
                e.printStackTrace();
            }



        }
    }


    @Override
    public void sendEmailNotification(EmailRequest sendEmailRequest) throws CustomCheckedException {
        Context context = new Context();
        context.setVariable("mailSubject", "Contact Redwood");
        context.setVariable("customMessage", sendEmailRequest.getMessage());
        context.setVariable("customerEmail",sendEmailRequest.getEmail());
        context.setVariable("customerAddress",sendEmailRequest.getAddress());
        context.setVariable("customerName",sendEmailRequest.getName());
        String toAddresses ="info@redwoodaml.com";
        List<String> toAddressList = new ArrayList<>();
        if(toAddresses.contains(",")) {
            String[] parts = toAddresses.split(",");
            toAddressList = Arrays.stream(parts).collect(Collectors.toList());
        }else toAddressList.add(toAddresses);
        for(String customer :  toAddressList) {
            String templateLocation = this.getTemplateLocation("redwood");
            String content = templateEngine.process(templateLocation, context);
            Email email = EmailBuilder.startingBlank()
                    .from(redWoodSenderName, redWoodSenderEmail)
                    .to(null, toAddressList)
                    .withSubject("Contact RedWood")
                    .withHTMLText(content).buildEmail();
            if(notificationsEnabled) {
                try {
                    if(!emailService.isEmailExcluded(sendEmailRequest.getAddress())) {

                        Mailer mailer = MailerBuilder.withSMTPServerHost(mailUrl)
                                .withSMTPServerPort(mailPort)
                                .withSMTPServerUsername(mailUser)
                                .withSMTPServerPassword(mailPass)
                                .withTransportStrategy(TransportStrategy.SMTP_TLS).buildMailer();
                                 mailer.sendMail(email, async);
                               // mailer.sendMail(email, async);
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
                        Mailer mailer = MailerBuilder.withSMTPServerHost(mailUrl)
                                .withSMTPServerPort(mailPort)
                                .withSMTPServerUsername(mailUser)
                                .withSMTPServerPassword(mailPass)
                                .withTransportStrategy(TransportStrategy.SMTP_TLS).buildMailer();
                                mailer.sendMail(email, async);
                      //  mailer.sendMail(email, async);
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
            case "pendingDocument":
                return "email/pendingdoc-noti";
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
            case "regotp":
                return "email/regotp";
            case "tranotp":
                return "email/tranotp";

            case "disbursefailed":
                return "email/disbursefailed";

            case "mandateActivated":
                return "email/mandateActivated";

            case "broadcastredwood":
                return "email/redwoodbroadcast";

            case "staffemail":
                return "email/staffemail";

            case "middlewareMonitor":
                return "email/middleware-monitor";

            case "onboardCustomer":
                return "email/complete_registration";

            case "loanrequest":
                return "email/webisites-loan-mail";

            case "sendMessage":
                return "email/send-message";

            case "contactus":
                return "email/contactus";

            case "fund-transfer":
                return "email/fund-transfer";

            case "electric-transfer":
                return "email/electric-transfer";

            case "exception":
                return "email/exception";

            case "withdrawal-request":
                return "email/withdrawal";

            case "nibbs-settlement-transaction":
                return "email/nibbs-settlement";

            case "savings-transfer":
                return "email/savings-transfer";


            case "bulk-initiated":
                return "email/bulk-initiated";

            case "bulk-transaction":
                return "email/bulk-transaction";

            case "bulk-approval":
                return "email/bulk-approval";

            case "website-contact-us":
                return "email/contact-us-email";

            case "approved":
                return "email/approved";

            case "rejected":
                return "email/rejected";
            case "accountStatement":
                return "email/accountStatement";
            case "LoginNotification":
                return "email/loginEmail";

            default:
                throw new CustomCheckedException("Invalid template name provided");
        }
    }

    private List<Recipient> getAllRecipient(List<To> requestTo, List<To> requestCC, List<To> requestBcc){
        var to = toRecipient(requestTo);
        List<Recipient> cc = (requestCC != null) ? toCcRecipient(requestCC) : new ArrayList<>();
        List<Recipient> bcc = (requestBcc != null) ? toBccRecipient(requestBcc) : new ArrayList<>();

        return Stream.of(to, cc, bcc)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<Recipient> toRecipient(List<To> tos){
        return tos.stream().map(to ->
                new Recipient(to.getName(), to.getEmail(), Message.RecipientType.TO))
                .collect(Collectors.toList());
    }

    private List<Recipient> toBccRecipient(List<To> tos){
        return tos.stream().map(to ->
                        new Recipient(to.getName(), to.getEmail(), Message.RecipientType.BCC))
                .collect(Collectors.toList());
    }

    private List<Recipient> toCcRecipient(List<To> tos){
        return tos.stream().map(to ->
                        new Recipient(to.getName(), to.getEmail(), Message.RecipientType.CC))
                .collect(Collectors.toList());
    }

    public void processSendingEmail(Map<String, Object> maps, List<Recipient> tos, String subject, String fromName, String fromEmail, String templateName) throws CustomCheckedException {
        if(!notificationsEnabled) throw new CustomCheckedException("Notification not enabled");

        Context context = new Context();
        maps.forEach(context::setVariable);

        String content = templateEngine.process(templateName, context);

        Email email = EmailBuilder.startingBlank()
                .from(new Recipient(fromName, fromEmail, Message.RecipientType.TO))
                .to(tos)
                .withSubject(subject)
                .withHTMLText(content)
                .buildEmail();

        sendMail(email);
    }

    public AsyncResponse sendMail(Email email){
        Mailer mailer = MailerBuilder.withSMTPServerHost(mailUrl)
                .withSMTPServerPort(mailPort)
                .withSMTPServerUsername(mailUser)
                .withSMTPServerPassword(mailPass)
                .withTransportStrategy(TransportStrategy.SMTP_TLS).buildMailer();

        return mailer.sendMail(email, async);
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
