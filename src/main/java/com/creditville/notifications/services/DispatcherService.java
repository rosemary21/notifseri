package com.creditville.notifications.services;

import java.time.LocalDate;

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

    void performMiscOperation(LocalDate startDate, LocalDate endDate);
}
