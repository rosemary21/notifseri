package com.creditville.notifications.sms.services.implementation;

import com.creditville.notifications.executor.HttpCallService;
import com.creditville.notifications.sms.dto.bulksms.SmsTextDto;
import com.creditville.notifications.sms.dto.bulksms.SmsTextResponse;
import com.creditville.notifications.sms.services.BulkSmsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BulkSmsServiceImpl implements BulkSmsService {
    @Value("${sender.sms.send}")
    private String smsSender;

    @Value("${bulk.sms.send.url}")
    private String singleSmsUrl;

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    HttpCallService httpCallService;


    public SmsTextResponse sendSms(SmsTextDto requestDto){
        String payload = null;
        SmsTextResponse resp = null;

       requestDto.setSender_name(smsSender);

        try {
            payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestDto);
            log.info("ENTRY payload -> request {} ",payload);

        String response = httpCallService.doBasicSmsPost(singleSmsUrl,payload);
        log.info("ENTRY response -> sendSms {} ", response);

            resp = objectMapper.reader().forType(SmsTextResponse.class).readValue(response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return resp;

    }

    public SmsTextResponse sendMultipleSms(SmsTextDto requestDto){
        String payload = null;
        SmsTextResponse resp = null;
        requestDto.setSender_name(smsSender);
       //var response= requestDto.getRecipients().split(",");

        try{
            payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestDto);
            log.info("ENTRY payload -> request {} ",payload);

            String response = httpCallService.doBasicSmsPost(singleSmsUrl,payload);
            log.info("ENTRY response -> sendSms {} ", response);

            resp = objectMapper.reader().forType(SmsTextResponse.class).readValue(response);

        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return resp;
    }
}
