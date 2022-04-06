package com.creditville.notifications.models;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Data
public class CardTransactions {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String cardType;
    private BigDecimal amount;
    private String transactionDate;
    private String currency;

    private String reference;
    private String status;
//    @Lob
    private String paystackResponse;
    @Lob
    private String instafinResponse;

    @ManyToOne
    private CardDetails cardDetails;
    private LocalDate lastUpdate = LocalDate.now();
    @ManyToOne
    private Mandates mandates;
    @Transient
    private String gatewayResponse;

    private String mandateId;
    private String rrr;
    private String remitaRequestId;
    @Lob
    private String remitaResponse;

    private Date createdOn = new Date();

    @Enumerated(value = EnumType.STRING)
    private Channel tranType;
    private BigDecimal paystackFee;
    private BigDecimal totalDebitAmount;
}
