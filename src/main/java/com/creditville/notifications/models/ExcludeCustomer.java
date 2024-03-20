package com.creditville.notifications.models;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class ExcludeCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String loanId;
    private boolean isExcludedFromDebit = false;
    private  String modeOfPaymentType;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;

}
