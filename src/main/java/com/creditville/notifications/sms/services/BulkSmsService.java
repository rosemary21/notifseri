package com.creditville.notifications.sms.services;

import com.creditville.notifications.sms.dto.bulksms.SmsTextDto;
import com.creditville.notifications.sms.dto.bulksms.SmsTextResponse;

public interface BulkSmsService {

    SmsTextResponse sendSms(SmsTextDto requestDto);
}
