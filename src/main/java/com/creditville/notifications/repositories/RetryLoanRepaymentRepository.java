package com.creditville.notifications.repositories;

import com.creditville.notifications.models.RetryLoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetryLoanRepaymentRepository extends JpaRepository<RetryLoanRepayment, Long> {

    RetryLoanRepayment findByReferenceAndLoanIdAndClientId(String reference,String LoanId,String ClientId);

    RetryLoanRepayment findByReferenceAndMandateId(String reference,String mandatid);

    List<RetryLoanRepayment> findByProcessFlag(String flag);

    List<RetryLoanRepayment> findByProcessFlagAndManualStatus(String processFlag,String manualStatus);

    RetryLoanRepayment findByLoanIdAndManualStatus(String loanId,String status);

   }
