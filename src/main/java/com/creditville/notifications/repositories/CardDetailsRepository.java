package com.creditville.notifications.repositories;

import com.creditville.notifications.models.CardDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface CardDetailsRepository extends JpaRepository<CardDetails, Long> {

    CardDetails findByClientIdAndEmail(String clientId, String Email);

    CardDetails findByEmail(String email);

    Page<CardDetails> findAllByStatusIn(Collection<String> statusList, Pageable pageable);

    CardDetails findByAuthorizationCode(String authorizationCode);
}
