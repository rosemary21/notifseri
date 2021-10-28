package com.creditville.notifications.services.impl;

/* Created by David on 07/06/2021 */

import com.creditville.notifications.models.Branch;
import com.creditville.notifications.repositories.BranchRepository;
import com.creditville.notifications.services.BranchService;
import com.creditville.notifications.exceptions.CustomCheckedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BranchServiceImpl implements BranchService {
    @Autowired
    private BranchRepository branchRepository;

    @Override
    public Branch createBranch(String branchName) throws CustomCheckedException {
        if(branchRepository.findByName(branchName) != null)
            throw new CustomCheckedException(String.format("Branch with name: %s does not exist", branchName));
        return branchRepository.save(new Branch(branchName));
    }

    @Override
    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    @Override
    public Branch getBranch(String branchName) throws CustomCheckedException {
        Branch branch = branchRepository.findByName(branchName);
        if(branch == null)
            throw new CustomCheckedException(String.format("Branch with name: %s does not exist", branchName));
        else return branch;
    }

    @Override
    public Branch getBranch(Long branchId) throws CustomCheckedException {
        Optional<Branch> branch = branchRepository.findById(branchId);
        if(branch.isPresent()) return branch.get();
        else throw new CustomCheckedException(String.format("Branch with ID: %s does not exist", branchId));
    }

    @Override
    public boolean deleteBranch(Long branchId) throws CustomCheckedException {
        Branch branch = this.getBranch(branchId);
        return branchRepository.deleteByBranchId(branch.getId()) > 0;
    }
}
