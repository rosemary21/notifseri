package com.creditville.notifications.repositories;

import com.creditville.notifications.models.BranchManager;
import com.creditville.notifications.models.FailedEmail;
import com.creditville.notifications.models.FinanceManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinanceManagerRepository extends JpaRepository<FinanceManager, Long> {
    FinanceManager findByBranch(String branch);

}
