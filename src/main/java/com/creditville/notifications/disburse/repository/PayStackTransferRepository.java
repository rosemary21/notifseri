package com.creditville.notifications.disburse.repository;

import com.creditville.notifications.disburse.model.PayStackTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface PayStackTransferRepository  extends JpaRepository<PayStackTransfer, Long> {
    PayStackTransfer findByAccountNumberAndLoanAccountIdAndTransactionStatus(String accountNumber,String loanid,String transactionstatus);
    PayStackTransfer findByAccountNumberAndLoanAccountIdAndClientIdAndTransactionStatus(String accountNumber,String loanid,String clientid,String transactionstatus);
    PayStackTransfer findByReferenceCode(String referenceCode);
    PayStackTransfer findByAccountNumberAndLoanAccountIdAndClientId(String accountNumber,String loanid,String clientid);

}
