package com.creditville.notifications.models.DTOs;

import lombok.Data;

import javax.persistence.Lob;
import javax.persistence.OneToMany;
import java.math.BigDecimal;
import java.util.Collection;

@Data
public class CardDetailsDto {

    private String clientId;
    private BigDecimal amount;
    private String firstName;
    private String lastName;
    private String email;
    private String accountName;
    private String reference;
    private String channel;
    private String status;
    private BigDecimal cardTokenCharge;
    private String authorizationCode;
    private String signature;
//    @Lob
    private String paystackResponse;

//    @OneToMany
//    private Collection<CardTransactionsDto> cardTransactionsDtos;
}
