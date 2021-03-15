package com.creditville.notifications.repositories;

import com.creditville.notifications.models.PartialDebit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface PartialDebitRepository extends JpaRepository<PartialDebit, Long> {
    PartialDebit findByAuthorizationCodeAndAmountAndEmail(String authCode, BigDecimal amount, String email);
}
