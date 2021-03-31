package com.creditville.notifications.services;

import com.creditville.notifications.models.RecoveryOfficer;
import com.creditville.notifications.exceptions.CustomCheckedException;

import java.util.List;

public interface RecoveryOfficerService {
    RecoveryOfficer getRecoveryOfficer(String branch);

    RecoveryOfficer getRecoveryOfficer(Long collectionOfficerId) throws CustomCheckedException;

    List<RecoveryOfficer> getAllRecoveryOfficers();
}
