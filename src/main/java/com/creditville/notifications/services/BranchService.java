package com.creditville.notifications.services;

/* Created by David on 07/06/2021 */

import com.creditville.notifications.models.Branch;
import com.creditville.notifications.exceptions.CustomCheckedException;

import java.util.List;

public interface BranchService {
    Branch createBranch(String branchName) throws CustomCheckedException;

    Branch getBranch(String branchName) throws CustomCheckedException;

    Branch getBranch(Long branchId) throws CustomCheckedException;

    List<Branch> getAllBranches();

    boolean deleteBranch(Long branchId) throws CustomCheckedException;
}
