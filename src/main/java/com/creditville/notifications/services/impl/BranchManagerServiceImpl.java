package com.creditville.notifications.services.impl;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.BranchManager;
import com.creditville.notifications.repositories.BranchManagerRepository;
import com.creditville.notifications.services.BranchManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BranchManagerServiceImpl implements BranchManagerService {
    @Autowired
    private BranchManagerRepository branchManagerRepository;

    @Override
    public BranchManager getBranchManager(String branch) {
        return branchManagerRepository.findByBranch(branch);
    }

    @Override
    public BranchManager getBranchManager(Long branchManagerId) throws CustomCheckedException {
        Optional<BranchManager> branchManager = branchManagerRepository.findById(branchManagerId);
        if(branchManager.isPresent()) return branchManager.get();
        else throw new CustomCheckedException(String.format("Branch Manager with id %d does not exist", branchManagerId));
    }

    @Override
    public BranchManager createNew(String branch, String officerName, String officerEmail, String officerPhoneNo,String accountNumber,String accountName, String bankName) {
        return branchManagerRepository.save(new BranchManager(branch, officerName, officerEmail, officerPhoneNo,accountNumber,accountName,bankName));
    }

    @Override
    public List<BranchManager> getAllBranchManagers() {
        return branchManagerRepository.findAll();
    }
}
