package com.creditville.notifications.repositories;

import com.creditville.notifications.models.PartialDebit;
import com.creditville.notifications.models.PartialDebitAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PartialDebitAttemptRepository extends JpaRepository<PartialDebitAttempt, Long> {
    PartialDebitAttempt findByPartialDebitAndDate(PartialDebit partialDebit, LocalDate date);

    List<PartialDebitAttempt> findAllByPartialDebit(PartialDebit partialDebit);
}
