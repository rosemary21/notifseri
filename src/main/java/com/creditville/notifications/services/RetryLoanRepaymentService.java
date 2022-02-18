package com.creditville.notifications.services;

import com.creditville.notifications.models.CardTransactions;
import com.creditville.notifications.models.DTOs.RetryLoanRepaymentDTO;
import com.creditville.notifications.models.RetryLoanRepayment;

public interface RetryLoanRepaymentService {

    String saveRetryLoan(CardTransactions cardTransactions,RetryLoanRepaymentDTO retryLoanRepaymentDTO,String loandid);

    RetryLoanRepaymentDTO getLoanRepayment(CardTransactions cardTransactions,String loanId, String email,String instafinOblDate);

    String updateRetryLoan(RetryLoanRepayment retryLoanRepayment);

    String repaymentOfLoan(RetryLoanRepayment retryLoanRepayment);
}
