package com.creditville.notifications.models.DTOs;


import com.creditville.notifications.models.CardDetails;
import lombok.Data;

import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

@Data
public class CardTransactionsDto {

    private String cardType;
    private BigDecimal amount;
    private String transactionDate;
    private String currency;
    private String reference;
    private String status;
//    @Lob
    private String paystackResponse;
    private String remitaResponse;
    private String mandateId;
    private String rrr;
    private String remitaRequestId;
    private String instafinResponse;
    private BigDecimal paystackFee;
    private BigDecimal totalDebitAmount;

//    @ManyToOne
    private CardDetails cardDetails;
}
