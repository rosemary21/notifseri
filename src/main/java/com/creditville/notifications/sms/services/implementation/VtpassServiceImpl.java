package com.creditville.notifications.sms.services.implementation;

import com.creditville.notifications.executor.HttpCallService;
import com.creditville.notifications.sms.dto.bulksms.SmsTextDto;
import com.creditville.notifications.sms.dto.bulksms.SmsTextResponse;
import com.creditville.notifications.sms.dto.vtpass.VtpassRequestDto;
import com.creditville.notifications.sms.dto.vtpass.VtpassResponseDto;
import com.creditville.notifications.sms.services.VtpassService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VtpassServiceImpl implements VtpassService {
    @Value("${sender.sms.send}")
    private String smsSender;

    @Value("${vtpass.sms.send.url}")
    private String singleSmsUrl;

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    HttpCallService httpCallService;


    public VtpassResponseDto sendSms(VtpassRequestDto requestDto){
        String payload = null;
        VtpassResponseDto resp = null;

        requestDto.setSender(smsSender);
        requestDto.setResponsetype("json");

        try {
            payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestDto);
            log.info("ENTRY payload -> request {} ",payload);

            String response = httpCallService.doBasicSmsPost(singleSmsUrl,payload);
            log.info("ENTRY response -> sendSms {} ", response);

            resp = objectMapper.reader().forType(VtpassResponseDto.class).readValue(response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return resp;

    }
}
