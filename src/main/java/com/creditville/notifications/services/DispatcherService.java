package com.creditville.notifications.services;

import com.creditville.notifications.exceptions.CustomCheckedException;

import java.time.LocalDate;

/**
 * Created by Chuks on 02/09/2021.
 */
public interface DispatcherService {
    void performDueRentalOperation();

    void performDueRentalTwoOperation();

    void performDueRentalThreeOperation();

    void performArrearsOperation();

    void sendAllCient() throws Exception;

    void performPostMaturityOperation();

    void performChequeLodgementOperation();

    void performRecurringChargesOperation();

    void performRetryInstafinPayment();

    void performRecurringMandateDebitInstruction();

    void performMiscOperation(LocalDate startDate, LocalDate endDate);

    void notifyTeamOfOperation() throws CustomCheckedException;

    void sendOutEidNotification();

    void checkStatusAndUpdateMandate();
    void sendInstafinEmailBroadcast();
}
