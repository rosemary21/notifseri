package com.creditville.notifications.services;

/**
 * Created by Chuks on 02/09/2021.
 */
public interface DispatcherService {
    void performDueRentalOperation();

    void performDueRentalTwoOperation();

    void performDueRentalThreeOperation();

    void performArrearsOperation();

    void performPostMaturityOperation();

    void performChequeLodgementOperation();

    void performRecurringChargesOperation();
}
