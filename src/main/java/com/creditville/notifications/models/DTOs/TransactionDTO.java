package com.creditville.notifications.models.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long customerId;
    private Long paymentId;
    private String channel;
    @NotNull(message = "account number is required")
    private String accountNumber;
    private String bankAccountType;
    @NotNull(message = "transaction type is required")
    private String type;
    private String currency;
    @NotNull(message = "amount is required")
    private BigDecimal amount;
    private String status;
    private String narration;
    private String systemNarration;
    private String reference;
    private String transactionId;
    private BigDecimal balanceAfter;
    private LocalDate transactionDate;
}
