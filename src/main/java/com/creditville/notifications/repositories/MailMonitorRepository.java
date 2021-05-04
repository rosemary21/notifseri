package com.creditville.notifications.repositories;

import com.creditville.notifications.models.MailMonitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MailMonitorRepository extends JpaRepository<MailMonitor, Long> {
    Optional<MailMonitor> findByOperationNameAndEventDate(String operationName, LocalDate eventDate);

    List<MailMonitor> findAllByEventDate(LocalDate eventDate);
}
