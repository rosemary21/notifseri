package com.creditville.notifications.disburse.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class SendMailRequest implements Serializable  {
    private String mailSubject;
    private String mailMessage;
    private ObjectNode mailData;
    private String mailTemplate;

    public SendMailRequest(String mailSubject, ObjectNode mailData, String mailTemplate) {
        this.mailSubject = mailSubject;
        this.mailData = mailData;
        this.mailTemplate = mailTemplate;
    }

    public SendMailRequest(String mailSubject, String mailMessage , ObjectNode mailData, String mailTemplate) {
        this.mailSubject = mailSubject;
        this.mailMessage = mailMessage;
        this.mailData = mailData;
        this.mailTemplate = mailTemplate;
    }
}
