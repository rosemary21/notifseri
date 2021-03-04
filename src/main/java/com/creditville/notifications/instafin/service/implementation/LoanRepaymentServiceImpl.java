package com.creditville.notifications.instafin.service.implementation;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.executor.HttpCallService;
import com.creditville.notifications.instafin.req.RepayLoanReq;
import com.creditville.notifications.instafin.service.LoanRepaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LoanRepaymentServiceImpl implements LoanRepaymentService {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private HttpCallService httpCallService;

    @Value("${instafin.base.url}")
    private String baseUrl;
    @Value("${instafin.loan.repayment.url}")
    private String loanRepayUrl;

    @Override
    public String makeLoanRepayment(RepayLoanReq repayLoanReq) {
        try {
            var payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(repayLoanReq);
            System.out.println("payload: "+payload);
            var repayLoanResp = httpCallService.doBasicPost(baseUrl+loanRepayUrl,payload);
            System.out.println("repayLoanResp: "+repayLoanResp);
            return repayLoanResp;

        } catch (JsonProcessingException | CustomCheckedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
