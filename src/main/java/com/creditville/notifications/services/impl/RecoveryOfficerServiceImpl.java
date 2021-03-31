package com.creditville.notifications.services.impl;

import com.creditville.notifications.models.RecoveryOfficer;
import com.creditville.notifications.repositories.RecoveryOfficerRepository;
import com.creditville.notifications.services.RecoveryOfficerService;
import com.creditville.notifications.exceptions.CustomCheckedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecoveryOfficerServiceImpl implements RecoveryOfficerService {
    @Autowired
    private RecoveryOfficerRepository recoveryOfficerRepository;

    @Override
    public RecoveryOfficer getRecoveryOfficer(String branch) {
        return recoveryOfficerRepository.findByBranch(branch);
    }

    @Override
    public RecoveryOfficer getRecoveryOfficer(Long RecoveryOfficerId) throws CustomCheckedException {
        Optional<RecoveryOfficer> RecoveryOfficer = recoveryOfficerRepository.findById(RecoveryOfficerId);
        if(RecoveryOfficer.isPresent()) return RecoveryOfficer.get();
        else throw new CustomCheckedException(String.format("Recovery officer with id %d does not exist", RecoveryOfficerId));
    }

    @Override
    public List<RecoveryOfficer> getAllRecoveryOfficers() {
        return recoveryOfficerRepository.findAll();
    }
}
