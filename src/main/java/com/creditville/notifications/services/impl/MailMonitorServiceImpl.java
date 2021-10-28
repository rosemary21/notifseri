package com.creditville.notifications.services.impl;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.MailMonitor;
import com.creditville.notifications.repositories.MailMonitorRepository;
import com.creditville.notifications.services.MailMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MailMonitorServiceImpl implements MailMonitorService {
    @Autowired
    private MailMonitorRepository mailMonitorRepository;

    @Override
    public MailMonitor getDailyEvent(String operationName) throws CustomCheckedException {
        LocalDate today = LocalDate.now();
        Optional<MailMonitor> dailyEvent = mailMonitorRepository.findByOperationNameAndEventDate(operationName, today);
        if(dailyEvent.isPresent()) return dailyEvent.get();
        else throw new CustomCheckedException(String.format("No mail monitor event found for today (%s)", today.toString()));
    }

    private MailMonitor createDailyEvent(String operationName) {
        LocalDate today = LocalDate.now();
        return mailMonitorRepository.save(new MailMonitor(operationName, today));
    }

    @Override
    public MailMonitor modifyDailyMonitor(String operationName, Long successCount, Long failureCount) throws CustomCheckedException {
        MailMonitor dailyMailMonitor;
        try {
            this.getDailyEvent(operationName);
        }catch (CustomCheckedException cce) {
            this.createDailyEvent(operationName);
        }
        dailyMailMonitor = this.getDailyEvent(operationName);
        dailyMailMonitor.setSuccessCount(successCount);
        dailyMailMonitor.setFailedCount(failureCount);
        return mailMonitorRepository.save(dailyMailMonitor);
    }

    @Override
    public List<MailMonitor> getAllDailyEventOperations() {
        LocalDate today = LocalDate.now();
        return mailMonitorRepository.findAllByEventDate(today);
    }
}
