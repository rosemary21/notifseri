package com.creditville.notifications.sms.dto.bulksms;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsTextDto {

    private String message;
    private String sender_name;
    private String recipients;
}
