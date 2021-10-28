package com.creditville.notifications.models;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"clientId", "loanId"}))
@Data
@Entity
public class Mandates {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String clientId;
    private String loanId;
    private String hash;
    private String name;
    private String email;
    private String phoneNumber;
    private String bankCode;
    private String account;
    private String requestId;
    private String mandateId;
    private BigDecimal amount;
    private String startDate;
    private String endDate;
    private LocalDateTime createdOn;
    private String mandateType;
//    private String frequency;
    private String statusCode;
    private String status;
    private String activationStatus;
    private String remitaResponse;
    private String remitaTransRef;
    private String maxNoOfDebits;
    private String mandateForm;
}
