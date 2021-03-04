package com.creditville.notifications.repositories;

import com.creditville.notifications.models.FailedEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * Created by Chuks on 02/09/2021.
 */
@Repository
public interface FailedEmailRepository extends JpaRepository<FailedEmail, Long> {
    FailedEmail findByToAddressAndSubjectAndPaymentDate(String toAddress, String subject, LocalDate paymentDate);
}
