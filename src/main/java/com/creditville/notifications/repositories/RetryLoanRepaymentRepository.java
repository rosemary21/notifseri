package com.creditville.notifications.repositories;

import com.creditville.notifications.models.RetryLoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RetryLoanRepaymentRepository extends JpaRepository<RetryLoanRepayment, Long> {
}
