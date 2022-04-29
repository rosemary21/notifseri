package com.creditville.notifications.services;


import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.BranchManager;

import java.util.List;

public interface BranchManagerService {
    BranchManager getBranchManager(String branch);

    BranchManager getBranchManager(Long branchManagerId) throws CustomCheckedException;

    BranchManager createNew(String branch, String officerName, String officerEmail, String officerPhoneNo,String accountNumber,String accountName, String bankName);
    List<BranchManager> getAllBranchManagers();
}
