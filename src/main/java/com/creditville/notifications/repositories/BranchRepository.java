package com.creditville.notifications.repositories;

/* Created by David on 07/06/2021 */

import com.creditville.notifications.models.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    Branch findByName(String name);

    @Transactional
    @Modifying
    @Query("DELETE FROM Branch b WHERE b.id =:branchId")
    int deleteByBranchId(@Param("branchId") Long branchId);
}