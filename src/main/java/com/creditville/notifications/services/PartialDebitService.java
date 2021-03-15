package com.creditville.notifications.services;

import com.creditville.notifications.models.PartialDebit;
import com.creditville.notifications.models.PartialDebitAttempt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PartialDebitService {
    PartialDebit savePartialDebit(String authCode, String loanId, BigDecimal amount, String email, LocalDate paymentDate);

    PartialDebit getPartialDebit(String authCode, BigDecimal amount, String email);

    PartialDebitAttempt getPartialDebitAttempt(PartialDebit partialDebit, LocalDate date);

    PartialDebitAttempt savePartialDebitAttempt(PartialDebitAttempt partialDebitAttempt);

    void deletePartialDebitRecord(Long partialDebitId);

    List<PartialDebit> getAllPartialDebitRecords();

    void performPartialDebitOp();
}
