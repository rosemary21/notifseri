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
    public CardTransactions addCardTransaction(CardTransactions cardTransactions) {
        return cardTransactionRepo.save(cardTransactions);
    }

    @Override
    public CardTransactions saveCardTransaction(CardTransactionsDto cardTransactionsDto) {
        return addCardTransaction(convertCardTransactionDtoToEntity(cardTransactionsDto));
    }

    private CardTransactions convertCardTransactionDtoToEntity(CardTransactionsDto ctDTO){
        return cardUtil.modelMapper().map(ctDTO, CardTransactions.class);
    }


}
