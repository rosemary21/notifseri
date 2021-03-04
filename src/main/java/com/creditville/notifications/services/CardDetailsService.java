package com.creditville.notifications.services;

import com.creditville.notifications.models.CardDetails;
import com.creditville.notifications.models.DTOs.ChargeDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CardDetailsService {
    public void saveCustomerCardDetails(CardDetails cardDetails);

    public void cardAuthorization(String response, String clientId);

    public void cardRecurringCharges(String email, BigDecimal amount, String loanId, LocalDate currentDate, String clientID);

    public String chargeCard(ChargeDto chargeDto);

    public String verifyTransaction(String reference);

    List<CardDetails> getAllCardDetails(Integer pageNo, Integer pageSize);
}
