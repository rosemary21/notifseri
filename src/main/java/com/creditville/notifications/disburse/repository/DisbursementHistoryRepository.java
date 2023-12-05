package com.creditville.notifications.disburse.repository;

import com.creditville.notifications.disburse.model.DisbursementHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisbursementHistoryRepository extends JpaRepository<DisbursementHistory, Long> {

    DisbursementHistory findByClientIdAndAccountId(String clientId, String accountId);

    List<DisbursementHistory> findAllByOrderByDateCreatedDesc();

    List<DisbursementHistory> findByStatusOrderByDateCreatedDesc(String status);

    DisbursementHistory findByReference(String reference);

    @Query("select s from DisbursementHistory s where s.accountId  like %:search% or s.clientId  like %:search% or s.statusDesc like %:search% or s.transactionId like %:search% ")
    List<DisbursementHistory> findBySearch(@Param("search") String search);

    @Query("select s from DisbursementHistory s where s.status=:status")
    List<DisbursementHistory> findByStatus(@Param("status") String status);

    @Query("select s from DisbursementHistory s where s.status=:status or s.status=:failStatus")
    List<DisbursementHistory> findByStatusAndFailedStatus(@Param("status") String status,@Param("failStatus") String failStatus);

    @Query("select s from DisbursementHistory s where s.status=:status and ( s.accountId  like %:search% or s.clientId  like %:search% or s.statusDesc like %:search% or s.transactionId like %:search% )")
    List<DisbursementHistory> findByStatusSearch(@Param("search") String search, @Param("status") String status);

}
