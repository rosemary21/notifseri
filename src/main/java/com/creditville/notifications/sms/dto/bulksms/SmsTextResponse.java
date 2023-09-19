package com.creditville.notifications.sms.dto.bulksms;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsTextResponse {
    private String status;
    private String msgid;
    private String units;
    private String balance;
    private String msg;

}
