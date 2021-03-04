package com.creditville.notifications.repositories;

import com.creditville.notifications.models.EmailAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * Created by Chuks on 02/09/2021.
 */
@Repository
public interface EmailAuditRepository extends JpaRepository<EmailAudit, Long> {
    EmailAudit findByToAddressAndToNameAndSubjectAndPaymentDate(String toAddress, String toName, String subject, LocalDate paymentDate);
}
