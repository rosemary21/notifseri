package com.creditville.notifications.disburse.service.impl;

import com.creditville.notifications.disburse.dto.DisburseLoanResponse;
import com.creditville.notifications.disburse.dto.RequestDisburseDto;
import com.creditville.notifications.disburse.model.LookupDisburse;
import com.creditville.notifications.disburse.model.Payments;
import com.creditville.notifications.disburse.service.LoanDisbursementService;
import com.creditville.notifications.executor.HttpCallService;
import com.creditville.notifications.instafin.common.AppConstants;
import com.creditville.notifications.utils.DateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LoanDisbursementServiceImpl implements LoanDisbursementService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HttpCallService httpCallService;

    @Value("${instafin.loan.disbursement.url}")
    private String disburseLoanUrl;
    @Value("${instafin.base.url}")
    private String baseUrl;

    @Value("${instafin.loan.disbursement.url}")
    private String tgdisburseLoanUrl;
    @Value("${instafin.base.url}")
    private String tgbaseUrl;

    @Override
    public DisburseLoanResponse disburseLoanResponse(RequestDisburseDto requestDisburseDto){
        DisburseLoanResponse disburseLoanResponse=null;
        try{
            LookupDisburse lookupDisburse=initiateRequest(requestDisburseDto);
            String payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(lookupDisburse);
            if(requestDisburseDto.isTgValid()){
                String lookUpLoanAccountResp = httpCallService.doBasicPost((tgbaseUrl + tgdisburseLoanUrl), payload);
                disburseLoanResponse = objectMapper.readValue(lookUpLoanAccountResp, DisburseLoanResponse.class);
                return disburseLoanResponse;
            }
            if(!requestDisburseDto.isTgValid()){
                String lookUpLoanAccountResp = httpCallService.doBasicPost((baseUrl + disburseLoanUrl), payload);
                disburseLoanResponse = objectMapper.readValue(lookUpLoanAccountResp, DisburseLoanResponse.class);
                return disburseLoanResponse;
            }
           return null;
        }
        catch (Exception e){
            e.printStackTrace();
            return disburseLoanResponse;
        }
    }



    private LookupDisburse initiateRequest(RequestDisburseDto requestDisburseDto){
        LookupDisburse lookupDisburse=new LookupDisburse();
        Payments payments=new Payments();
        List<Payments> listPayment=new ArrayList<>();
        payments.setAmount(requestDisburseDto.getAmount());
//        payments.setPaymentMethodName("Cash");

        if(requestDisburseDto.isTgValid()){
            payments.setPaymentMethodName(AppConstants.InstafinPaymentMethod.TG_PAYSTACK_PAYMENT_METHOD);
        }
        if(!requestDisburseDto.isTgValid()){
            payments.setPaymentMethodName(AppConstants.InstafinPaymentMethod.AUTO_DISBURSE_PAYMENT_METHOD);

        }
        lookupDisburse.setAccountID(requestDisburseDto.getLoanAccount());
        lookupDisburse.setDisbursementDate(DateUtil.currentDate());
        listPayment.add(payments);
        lookupDisburse.setPayments(listPayment);
        lookupDisburse.setFirstRepaymentDate(requestDisburseDto.getFirstRepaymentMethod());
        return lookupDisburse;

    }
}
