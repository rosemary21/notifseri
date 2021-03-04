package com.creditville.notifications.services.impl;

import com.creditville.notifications.models.CardTransactions;
import com.creditville.notifications.models.DTOs.CardTransactionsDto;
import com.creditville.notifications.repositories.CardTransactionRepository;
import com.creditville.notifications.services.CardTransactionsService;
import com.creditville.notifications.utils.CardUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CardTransactionServiceImpl implements CardTransactionsService {

    @Autowired
    private CardTransactionRepository cardTransactionRepo;
    @Autowired
    private CardUtil cardUtil;

    @Override
    public void addCardTransaction(CardTransactions cardTransactions) {
        cardTransactionRepo.save(cardTransactions);
    }

    @Override
    public void saveCardTransaction(CardTransactionsDto cardTransactionsDto) {
        addCardTransaction(convertCardTransactionDtoToEntity(cardTransactionsDto));
    }

    private CardTransactions convertCardTransactionDtoToEntity(CardTransactionsDto ctDTO){
        return cardUtil.modelMapper().map(ctDTO, CardTransactions.class);
    }


}
