package com.creditville.notifications.repositories;

import com.creditville.notifications.models.Branch;
import com.creditville.notifications.models.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BroadCastRepository  extends JpaRepository<EmailTemplate, Long> {
  EmailTemplate findBySender(String sender);

}
