package com.creditville.notifications.disburse.model;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Table(name = "disbursment_history")
@Entity
@Data
public class DisbursementHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String disbursementDate;
    private String accountId;
    private String firstRepaymentDate;
    private String accountName;
    private String transactionId;
    private String modeOfRepayment;
    private String repaymentBank;
    private Date dateCreated;
    private String status;
    private String statusDesc;
    private String clientName;
    private Boolean topUpStatus;
    private String clientId;
    private String loanId;
    private String loanAccount;
    private Integer retryCount;
    private String firstRepaymentMethod;
    private BigDecimal amount;
    private String approveUsername;
    private String repaymentAccountNo;
    private BigDecimal activeAmount;
    private String bankCode;
    @Type(type = "org.hibernate.type.TextType")
    @Lob
    private String payLoadRequest;
    private String remitaState;
    private String activeLoanId;
    private BigDecimal amountWithInterest;
    private String initiatedUsername;
    private Date approveDate;

    @Type(type = "org.hibernate.type.TextType")
    @Lob
    private String payLoadResponse;

    @OneToMany(cascade = CascadeType.ALL)
    private List<DisbursementPayment> payments;
    private String reference;


    @Override
    public String toString() {
        return "DisbursementHistory{" +
                "id=" + id +
                ", disbursementDate='" + disbursementDate + '\'' +
                ", accountId='" + accountId + '\'' +
                ", firstRepaymentDate='" + firstRepaymentDate + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", dateCreated=" + dateCreated +
                ", status='" + status + '\'' +
                ", clientId='" + clientId + '\'' +
                ", payments=" + payments +
                '}';
    }
}
