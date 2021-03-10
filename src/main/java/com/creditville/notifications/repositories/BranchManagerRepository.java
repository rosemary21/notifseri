package com.creditville.notifications.repositories;

import com.creditville.notifications.models.BranchManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchManagerRepository extends JpaRepository<BranchManager, Long> {
    BranchManager findByBranch(String branch);
}
