package com.creditville.notifications.disburse.model;

import lombok.Data;

import javax.persistence.*;

@Table(name = "disbursment_payment")
@Entity
@Data
public class DisbursementPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String amount;
    private String paymentMethodName;
    private String paymentStatus;

}
