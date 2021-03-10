package com.creditville.notifications.repositories;

import com.creditville.notifications.models.CollectionOfficer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionOfficerRepository extends JpaRepository<CollectionOfficer, Long> {
    CollectionOfficer findByBranch(String branch);
}
