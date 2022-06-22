package com.creditville.notifications.services;

import com.creditville.notifications.models.CardDetails;
import com.creditville.notifications.models.DTOs.ChargeDto;
import com.creditville.notifications.models.DTOs.PartialDebitDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CardDetailsService {
    public void saveCustomerCardDetails(CardDetails cardDetails);

    public void cardAuthorization(String response, String clientId);

    void cardRecurringCharges(String email, BigDecimal amount, String loanId, LocalDate currentDate, String clientID,String instafinDate, BigDecimal charge);

    void initiateRemitaRecurringCharges(BigDecimal amount, String loanId, LocalDate currentDate, String clientID);

    String makePartialDebit(PartialDebitDto partialDebitDto, boolean isClientTG);

    public String chargeCard(ChargeDto chargeDto,  boolean isTGClient);

    public String verifyTransaction(String reference, boolean isClientTG);

    List<CardDetails> getAllCardDetails(Integer pageNo, Integer pageSize);
}
