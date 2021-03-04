package com.creditville.notifications.repositories;

import com.creditville.notifications.models.CardDetails;
import com.creditville.notifications.models.CardTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface CardTransactionRepository extends JpaRepository<CardTransactions, Long> {

    Collection<CardTransactions> findByCardDetails_Email(String email);

    CardTransactions findByCardDetailsAndStatusInAndLastUpdate(CardDetails cardDetails, List<String> status, LocalDate LastUpdateDate);
}
