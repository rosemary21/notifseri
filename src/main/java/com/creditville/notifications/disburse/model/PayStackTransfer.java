package com.creditville.notifications.disburse.model;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;

@Table(name = "pay_stack_transfer")
@Data
@Entity
public class PayStackTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String userName;
    private String dateCreated;
    private String transactionStatus;
    private String loanAccountId;
    private BigDecimal loanAmount;
    private String clientId;
    private String initiatedDate ;
    private String disbursementStatus;
    private String loanCurrency;
    private BigDecimal transferAmount;
    private String transferCurrency;
    private String accountNumber;
    private String accountName;
    private String bankid;
    private String referenceCode;
    private Long recipient;
    private String initiationReference;

    @Type(type = "org.hibernate.type.TextType")
    @Lob
    private String responseLoad;

    @Type(type = "org.hibernate.type.TextType")
    @Lob
    private String requestLoad;
}
