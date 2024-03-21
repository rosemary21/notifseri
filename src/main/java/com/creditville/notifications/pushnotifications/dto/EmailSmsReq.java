package com.creditville.notifications.pushnotifications.dto;

import com.creditville.notifications.models.requests.SendEmailRequest;
import com.creditville.notifications.sms.dto.SMSDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailSmsReq {

    private EmailOrSmsNotificationEnum emailOrSmsNotificationEnum;
    private SendEmailRequest emailRequest;
    private SMSDTO smsRequest;
}
