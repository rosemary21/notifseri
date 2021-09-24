package com.creditville.notifications.services;

import com.creditville.notifications.models.requests.HookEvent;

public interface TransactionService {
    void handlePaystackTransactionEvent(HookEvent hookEvent);

   void handleRemittaTransactionEvent(HookEvent hookEvent);
}
