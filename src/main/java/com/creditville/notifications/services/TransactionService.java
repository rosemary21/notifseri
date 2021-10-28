package com.creditville.notifications.services;

import com.creditville.notifications.models.requests.HookEvent;
import com.creditville.notifications.models.requests.RemitaHookEvent;

public interface TransactionService {
    void handlePaystackTransactionEvent(HookEvent hookEvent);

    void handleRemitaActivationEvent(RemitaHookEvent hookEvent);

    void handleRemitaDebitEvent(RemitaHookEvent hookEvent);
}
