package com.creditville.notifications.repositories;

import com.creditville.notifications.models.RecoveryOfficer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecoveryOfficerRepository extends JpaRepository<RecoveryOfficer, Long> {
    RecoveryOfficer findByBranch(String branch);
}
