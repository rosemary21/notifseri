package com.creditville.notifications.sms.controller;

import com.creditville.notifications.sms.dto.*;
import com.creditville.notifications.sms.services.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SmsController {

    @Autowired
    SmsService smsService;

    @RequestMapping(value = "/singlesms", method = RequestMethod.POST)
    public ResponseDTO getSingleSMS(@RequestBody SMSDTO requestDTO){
     System.out.println("getting the single sms"+requestDTO.toString());
     ResponseDTO responseDTO= smsService.sendSingleSms(requestDTO);
     System.out.println("getting the respons dto"+requestDTO);
     return responseDTO;

    }

    @RequestMapping(value = "/multiplesms", method = RequestMethod.POST)
    public MultipleResponseDTO getMutilpeSMS(@RequestBody MultipleSMSDTO requestDTO){
        System.out.println("getting the single sms"+requestDTO.toString());
        MultipleResponseDTO responseDTO= smsService.sendMultipleSms(requestDTO);
        System.out.println("getting the respons dto"+requestDTO);
        return responseDTO;

    }

    @RequestMapping(value = "/ticketid", method = RequestMethod.POST)
    public MultipleTicketResponseDTO checkStatus(@RequestBody TicketDTO requestDTO){
        MultipleTicketResponseDTO responseDTO= smsService.getListTickectId(requestDTO);
        System.out.println("getting the respons dto"+requestDTO);
        return responseDTO;
    }

    @RequestMapping(value = "/bulksms", method = RequestMethod.POST)
    public MultipleResponseDTO submitBulkSMS(@RequestBody BulkSMSDTO requestDTO){
        MultipleResponseDTO responseDTO= smsService.sendBulkSms(requestDTO);
        System.out.println("getting the respons dto"+requestDTO);
        return responseDTO;
    }


}
