package com.creditville.notifications.disburse.service;

import com.creditville.notifications.disburse.dto.DisburseLoanResponse;
import com.creditville.notifications.disburse.dto.RequestDisburseDto;

import java.math.BigDecimal;
import java.util.List;

public interface LoanDisbursementService {


    DisburseLoanResponse disburseLoanResponse(RequestDisburseDto requestDisburseDto);

}
