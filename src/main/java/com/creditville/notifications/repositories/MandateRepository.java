package com.creditville.notifications.repositories;

import com.creditville.notifications.models.Mandates;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MandateRepository extends JpaRepository<Mandates, Long> {

    Mandates findByClientId(String clientId);

    Mandates findByClientIdAndLoanId(String clientId, String loanId);

    Mandates findByMandateId(String mandateId);

    Mandates findByRemitaTransRef(String tranRef);

    Page<Mandates> findAllByStatusCode(String statusCode, Pageable pageable);
}
