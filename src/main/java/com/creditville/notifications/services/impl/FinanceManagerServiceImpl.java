package com.creditville.notifications.services.impl;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.Branch;
import com.creditville.notifications.models.FinanceManager;
import com.creditville.notifications.repositories.FinanceManagerRepository;
import com.creditville.notifications.services.FinanceManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FinanceManagerServiceImpl implements FinanceManagerService {

   @Autowired
   private FinanceManagerRepository financeManagerRepository;

    @Override
    public FinanceManager getBraManager(String branch) {
       return  financeManagerRepository.findByBranch(branch);
    }
}
