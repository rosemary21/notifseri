package com.creditville.notifications.repositories;

import com.creditville.notifications.models.EmailTemplate;
import com.creditville.notifications.models.SmsTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BroadCastSmsRepository extends JpaRepository<SmsTemplate, Long> {

    SmsTemplate findBySender(String sender);

}
