package com.creditville.notifications.services.impl;

import com.creditville.notifications.disburse.dto.DisburseLoanResponse;
import com.creditville.notifications.disburse.dto.DisbursePaymentResponse;
import com.creditville.notifications.disburse.dto.RequestDisburseDto;
import com.creditville.notifications.disburse.model.*;
import com.creditville.notifications.disburse.repository.DisbursementHistoryRepository;
import com.creditville.notifications.disburse.repository.PayStackTransferRepository;
import com.creditville.notifications.disburse.service.LoanDisbursementService;
import com.creditville.notifications.executor.HttpCallService;
import com.creditville.notifications.models.requests.HookEvent;
import com.creditville.notifications.services.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class TransferServiceImpl implements TransferService {

    RestTemplate restTemplate=new RestTemplate();

    @Autowired
    DisbursementHistoryRepository disbursementHistoryRepository;

    @Autowired
    PayStackTransferRepository payStackTransferRepository;

    @Value("${finance.email}")
    private String financeEmail;
    @Value("${fail.transfer}")
    private String failedTransferSubject;
    @Value("${reverse.transfer}")
    private String reverseTransferSubject;
    @Value("${app.notificationservice.url}")
    private String notificationServiceUrl;
    @Value("${paystack.base.url}")
    private String payStackurl;
    @Value("${paystack.listen.status}")
    private String listenStatusurl;
    @Value("${paystack.base.url}")
    private String tgpayStackurl;
    @Value("${paystack.listen.status}")
    private String tglistenStatusurl;
    @Value("${paystack.basic.auth}")
    private String payStackAuth;
    @Value("${loan.pattern}")
    private String loanPattern;
    @Value("${tg.paystack.basic.auth}")
    private String tgPayStackAuth;

    @Autowired
    LoanDisbursementService loanDisbursementService;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private HttpCallService httpCallService;


    public ListTransferStatus ListenTransferStatus(String code,boolean tgValid) {
        try
        {
            log.info("getting the reference code {}",code);
            org.springframework.http.HttpHeaders headers = new HttpHeaders();
            if(tgValid){
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(tgpayStackurl+tglistenStatusurl+code);
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                headers.setBearerAuth(tgPayStackAuth);
                HttpEntity<String> entity = new HttpEntity<String>(headers);
                ResponseEntity<ListTransferStatus> accountStatus= restTemplate.exchange(builder.toUriString(), HttpMethod.GET,entity,ListTransferStatus.class);
                return accountStatus.getBody();
            }
            if(!tgValid){
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(payStackurl+listenStatusurl+code);
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                headers.setBearerAuth(payStackAuth);
                HttpEntity<String> entity = new HttpEntity<String>(headers);
                ResponseEntity<ListTransferStatus> accountStatus= restTemplate.exchange(builder.toUriString(), HttpMethod.GET,entity,ListTransferStatus.class);
                return accountStatus.getBody();
            }
            return null;

        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public void disburseLoan(){
        try{
            List<DisbursementHistory> disbursementHistories=disbursementHistoryRepository.findByStatusAndFailedStatus("DP","DFFV");
            log.info("getting the disbursementHistories {} ",disbursementHistories.size());
            for(DisbursementHistory disbursementHistory:disbursementHistories){
                if(disbursementHistory.getReference()!=null){
                    if(!(disbursementHistory.getReference().isEmpty())){
                        boolean tgValue=  confirmLoanId(disbursementHistory.getClientId());
                        ListTransferStatus listTransferStatus=ListenTransferStatus(disbursementHistory.getReference(),tgValue);
                        if(listTransferStatus.getData().getStatus().equals("success")){
                            RequestDisburseDto requestDisburseDto=new RequestDisburseDto();
                            requestDisburseDto.setAmount(disbursementHistory.getAmount());
                            requestDisburseDto.setLoanAccount(disbursementHistory.getAccountId());
                            requestDisburseDto.setTgValid(tgValue);
                            log.info("getting the first repayment date {}",disbursementHistory.getFirstRepaymentMethod());
                            requestDisburseDto.setFirstRepaymentMethod(disbursementHistory.getFirstRepaymentMethod());
                            DisburseLoanResponse disburseLoanResponse =loanDisbursementService.disburseLoanResponse(requestDisburseDto);
                            log.info("gettig the disburse laon {}",disburseLoanResponse);
                            if(disburseLoanResponse!=null){
                                PayStackTransfer payStackTransfer=payStackTransferRepository.findByReferenceCode(disbursementHistory.getReference());
                                payStackTransfer.setTransactionStatus("S");
                                String payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(disburseLoanResponse);
                                String requestPayLoad = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
                                List<DisbursementPayment> disbursementPayments=new ArrayList<>();
                                for (DisbursePaymentResponse disbursePaymentResponse: disburseLoanResponse.getPayments()){
                                    DisbursementPayment disbursementPayment=new DisbursementPayment();
                                    log.info("getting the payment method {}",disbursePaymentResponse.getPaymentMethodName());
                                    disbursementPayment.setPaymentMethodName(disbursePaymentResponse.getPaymentMethodName());
                                    disbursementPayment.setAmount(disbursePaymentResponse.getAmount());
                                    disbursementPayments.add(disbursementPayment);
                                }
                                disbursementHistory.setPayments(disbursementPayments);
                                disbursementHistory.setPayLoadResponse(requestPayLoad);
                                disbursementHistory.setDisbursementDate(disburseLoanResponse.getDisbursementDate());
                                disbursementHistory.setFirstRepaymentDate(disburseLoanResponse.getFirstRepaymentDate());
                                disbursementHistory.setAccountId(disburseLoanResponse.getAccountID());
                                disbursementHistory.setTransactionId(disburseLoanResponse.getTransactionID());
                                disbursementHistory.setStatus("DVV");
                                disbursementHistory.setStatusDesc("Disbursement Done And Payment Verified");
                                disbursementHistoryRepository.save(disbursementHistory);
                                payStackTransferRepository.save(payStackTransfer);
                            }
                            if(disburseLoanResponse==null){
                                log.info("getting the retry count {}",disbursementHistory.getRetryCount());
                                if(disbursementHistory.getRetryCount()==null){
                                    disbursementHistory.setRetryCount(0);
                                }
                                int value=disbursementHistory.getRetryCount();
                                value= value+1;
                                disbursementHistory.setRetryCount(value);
                                disbursementHistory.setStatus("DFFV");
                                disbursementHistory.setStatusDesc("Disbursement Failed Payment Verified");
                                PayStackTransfer payStackTransfer=payStackTransferRepository.findByReferenceCode(disbursementHistory.getReference());
                                payStackTransfer.setTransactionStatus("S");
                                disbursementHistoryRepository.save(disbursementHistory);
                                payStackTransferRepository.save(payStackTransfer);
                                if(disbursementHistory.getRetryCount()<3){
                                    log.info("Sending Notification for failed transaction");
                                    disburseFailNotification(disbursementHistory);
                                }
                            }
                        }
                        if(listTransferStatus.getData().getStatus().equals("failed")){
                            PayStackTransfer payStackTransfer=payStackTransferRepository.findByReferenceCode(disbursementHistory.getReference());
                            payStackTransfer.setTransactionStatus("F");
                            int value=disbursementHistory.getRetryCount();
                            value= value+1;
                            disbursementHistory.setRetryCount(value);
                            disbursementHistory.setStatusDesc("Payment Failed And Disbursement Not Done");
                            disbursementHistory.setStatus("F");
                            disbursementHistoryRepository.save(disbursementHistory);
                            payStackTransferRepository.save(payStackTransfer);
                            log.info("getting the retry count {}",disbursementHistory.getRetryCount());
                            if(disbursementHistory.getRetryCount()<3){
                                transferFailNotification(disbursementHistory);
                            }
                        }
                    }

                }

            }
        }
       catch (Exception e){
            e.printStackTrace();
       }
    }


    public boolean confirmLoanId(String loanId){
        boolean value= loanId.startsWith(loanPattern);
        if(value){
            return true;
        }
        return  false;
    }


    public void transferFailNotification(DisbursementHistory disbursementHistory){
        ObjectNode notificationData = new ObjectNode(JsonNodeFactory.instance);
        notificationData.put("toAddress",financeEmail );
        notificationData.put("loanAmount", disbursementHistory.getPayments().get(0).getAmount());
        notificationData.put("loanId", disbursementHistory.getAccountId());
        notificationData.put("clientId", disbursementHistory.getClientId());
        try {
            SendMailRequest sendMailRequest = new SendMailRequest(failedTransferSubject, notificationData, "failedtranfer");
            var payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sendMailRequest);
            httpCallService.doBasicPost(notificationServiceUrl, payload);
        }catch (
                Exception jpe) {
            jpe.printStackTrace();
        }
    }

    public void disburseFailNotification(DisbursementHistory disbursementHistory){
        ObjectNode notificationData = new ObjectNode(JsonNodeFactory.instance);
        notificationData.put("toAddress",financeEmail );
        notificationData.put("loanAmount", disbursementHistory.getAmount());
        notificationData.put("loanId", disbursementHistory.getAccountId());
        notificationData.put("clientId", disbursementHistory.getClientId());
        try {
            SendMailRequest sendMailRequest = new SendMailRequest(failedTransferSubject, notificationData, "disbursefailed");
            var payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sendMailRequest);
            httpCallService.doBasicPost(notificationServiceUrl, payload);
        }catch (
                Exception jpe) {
            jpe.printStackTrace();
        }
    }


    public void transferReverseNotification(DisbursementHistory disbursementHistory){
        ObjectNode notificationData = new ObjectNode(JsonNodeFactory.instance);
        notificationData.put("toAddress",financeEmail );
        notificationData.put("loanAmount", disbursementHistory.getPayments().get(0).getAmount());
        notificationData.put("loanId", disbursementHistory.getAccountId());
        notificationData.put("clientId", disbursementHistory.getClientId());
        try {
            SendMailRequest sendMailRequest = new SendMailRequest(reverseTransferSubject, notificationData, "reversetranfer");
            var payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sendMailRequest);
            httpCallService.doBasicPost(notificationServiceUrl, payload);
        }catch (Exception jpe){
            jpe.printStackTrace();
        }
    }

}
