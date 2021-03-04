package com.creditville.notifications.repositories;

import com.creditville.notifications.models.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository  extends JpaRepository<Transactions, Long> {
}
