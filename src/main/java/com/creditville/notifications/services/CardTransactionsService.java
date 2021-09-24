package com.creditville.notifications.services;


import com.creditville.notifications.models.CardTransactions;
import com.creditville.notifications.models.DTOs.CardTransactionsDto;

public interface CardTransactionsService {

    CardTransactions addCardTransaction(CardTransactions cardTransactions);

    CardTransactions saveCardTransaction(CardTransactionsDto cardTransactionsDto);
}
