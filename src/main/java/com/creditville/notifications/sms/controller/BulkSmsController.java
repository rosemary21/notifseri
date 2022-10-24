package com.creditville.notifications.sms.controller;

import com.creditville.notifications.sms.dto.ResponseDTO;
import com.creditville.notifications.sms.dto.SMSDTO;
import com.creditville.notifications.sms.dto.bulksms.SmsTextDto;
import com.creditville.notifications.sms.dto.bulksms.SmsTextResponse;
import com.creditville.notifications.sms.services.BulkSmsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BulkSmsController {
    @Autowired
    private BulkSmsService bulkSmsService;
    @Autowired
    private ObjectMapper om;

    @RequestMapping(value = "/sendsms", method = RequestMethod.POST)
    public SmsTextResponse sendSMS(@RequestBody SmsTextDto requestDTO){
        SmsTextResponse response = bulkSmsService.sendSms(requestDTO);
        return response;

    }
}
