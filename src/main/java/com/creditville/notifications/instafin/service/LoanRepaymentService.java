package com.creditville.notifications.instafin.service;


import com.creditville.notifications.instafin.req.RepayLoanReq;

public interface LoanRepaymentService {

    String makeLoanRepayment(RepayLoanReq repayLoanReq);
}
