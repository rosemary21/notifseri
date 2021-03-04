package com.creditville.notifications.repositories;

import com.creditville.notifications.models.ExcludedEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Chuks on 02/09/2021.
 */
@Repository
public interface ExcludedEmailRepository extends JpaRepository<ExcludedEmail, Long> {
    ExcludedEmail findByEmailAddress(String emailAddress);

    @Transactional
    @Modifying
    @Query(value = "delete from ExcludedEmail k where k.id=:id")
    void deleteByExcludedEmailId(@Param("id") Long userId);
}
