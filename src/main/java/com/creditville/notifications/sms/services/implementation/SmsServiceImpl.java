package com.creditville.notifications.sms.services.implementation;

import com.creditville.notifications.sms.dto.*;
import com.creditville.notifications.sms.services.SmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Service
public class SmsServiceImpl implements SmsService {

    @Value("${vanso.url}")
    private String url;
    @Value("${vanso.username}")
    private String vansoUserName;

    @Value("${vanso.password}")
    private String vansoPassword;

    RestTemplate restTemplate=new RestTemplate();

    @Override
    public ResponseDTO sendSingleSms(SMSDTO requestDTO) {
        String singleSmsUrl=url+"/rest/sms/submit";
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(vansoUserName,vansoPassword);
        System.out.println("getting the vanso username"+vansoUserName);
        System.out.println("getting the vanso password"+vansoPassword);
//        System.out.println("getting the vanso object"+requestDTO.getSms().getText()+requestDTO.getSms().getSrc()+requestDTO.getSms().getText());
        Map<String, ResponseDTO> map = new HashMap<>();
        HttpEntity<SMSDTO> entity=new HttpEntity<SMSDTO>(requestDTO,headers);
        System.out.println("getting the single sms url"+singleSmsUrl);
        ResponseEntity<ResponseDTO> response = restTemplate.postForEntity(singleSmsUrl, entity, ResponseDTO.class);
        System.out.println("getting the response"+response.getBody().toString());
        return response.getBody();
    }

    @Override
    public MultipleResponseDTO sendMultipleSms(MultipleSMSDTO multipleSMSDTO) {

        String singleSmsUrl=url+"/rest/sms/submit/multi";
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(vansoUserName,vansoPassword);
        System.out.println("getting the vanso username"+vansoUserName);
        System.out.println("getting the vanso password"+vansoPassword);
        Map<String, ResponseDTO> map = new HashMap<>();
        HttpEntity<MultipleSMSDTO> entity=new HttpEntity<MultipleSMSDTO>(multipleSMSDTO,headers);
        System.out.println("getting the single sms url"+singleSmsUrl);
        ResponseEntity<MultipleResponseDTO> response = restTemplate.postForEntity(singleSmsUrl, entity, MultipleResponseDTO.class);
        System.out.println("getting the response"+response.getBody().toString());
        return response.getBody();
    }

    @Override
    public MultipleResponseDTO sendBulkSms(BulkSMSDTO smsdto) {
        String singleSmsUrl=url+"/rest/sms/submit/bulk";
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(vansoUserName,vansoPassword);
        System.out.println("getting the vanso username"+vansoUserName);
        System.out.println("getting the vanso password"+vansoPassword);
        Map<String, ResponseDTO> map = new HashMap<>();
        HttpEntity<BulkSMSDTO> entity=new HttpEntity<BulkSMSDTO>(smsdto,headers);
        System.out.println("getting the single sms url"+singleSmsUrl);
        ResponseEntity<MultipleResponseDTO> response = restTemplate.postForEntity(singleSmsUrl, entity, MultipleResponseDTO.class);
        System.out.println("getting the response"+response.getBody().toString());
        return response.getBody();
    }

    @Override
    public MultipleTicketResponseDTO getListTickectId(TicketDTO ticketDTO) {
        String singleSmsUrl=url+"/rest/sms/dlr";
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(vansoUserName,vansoPassword);
        System.out.println("getting the vanso username"+vansoUserName);
        System.out.println("getting the vanso password"+vansoPassword);
        Map<String, ResponseDTO> map = new HashMap<>();
        HttpEntity<TicketDTO> entity=new HttpEntity<TicketDTO>(ticketDTO,headers);
        System.out.println("getting the single sms url"+singleSmsUrl);
        ResponseEntity<MultipleTicketResponseDTO> response = restTemplate.postForEntity(singleSmsUrl, entity, MultipleTicketResponseDTO.class);
        System.out.println("getting the response"+response.getBody().toString());
        return response.getBody();
    }
}
