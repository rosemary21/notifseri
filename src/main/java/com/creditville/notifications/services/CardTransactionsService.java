package com.creditville.notifications.services;


import com.creditville.notifications.models.CardTransactions;
import com.creditville.notifications.models.DTOs.CardTransactionsDto;

public interface CardTransactionsService {

    public void addCardTransaction(CardTransactions cardTransactions);

    public void saveCardTransaction(CardTransactionsDto cardTransactionsDto);
}
